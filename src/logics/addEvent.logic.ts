import { ObjectId } from "mongodb";
import { ITimeBlock } from "../interfaces/events.interface";
import { EventModel } from "../models/event.model";
import { UserModel } from "../models/user.model";

export class AddEventLogic {
    userName: string;
    userEmail: string;
    blocks: ITimeBlock[];
    respWarning: any = "";
    conflictEventTitle: string = "";
    senderUserId: ObjectId;
    availableSlots: any[];
    constructor(
        protected newBlock: ITimeBlock,
        protected findInsertionPointLogic: any,
        protected userId: ObjectId
    ) {}

    async execute() {
        try {
            await this.checkSenderConflicts();
            await this.getBlocks();
            if(this.availableSlots?.length) {
                const sortedAvailableSlots = this.availableSlots.sort((a, b) => a.startTime - b.startTime);
                if(!(this.newBlock.startTime >= sortedAvailableSlots[0].startTime && this.newBlock.endTime <= sortedAvailableSlots[sortedAvailableSlots.length - 1].endTime)) {
                    this.respWarning = `You can't book a slot outside of the available timings`;
                    return this.respWarning;
                }
            }
            if (this.hasConflict()) {
                this.respWarning = `${this.userName} has a conflict with: ${this.conflictEventTitle}`;
                this.newBlock.isConflicted = true;
            }


            this.blocks.splice(this.findInsertionPointLogic.execute(this.newBlock.startTime), 0, this.newBlock);
            if(this.senderUserId) {
                /**
                 * If sender exists in calendly's db - add the same block to sender's blocks.
                 * Available timings validations is not required for sender
                 * default isAccepted is true
                 */
                this.newBlock.memberEmail = this.userEmail;
                this.newBlock.isAccepted = true;
                await EventModel.addBlock(this.senderUserId, this.newBlock);
            }
            this.newBlock.memberEmail = this.newBlock.senderEmail;
            this.newBlock.isAccepted = false;
            await EventModel.addBlock(this.userId, this.newBlock);
            /** send email: not integrated */
            return this.respWarning;
        } catch(e) {
            console.log(`Error in AddEventLogic.execute: ${e}`);
            throw e;
        }

    }

    protected hasConflict(): boolean {
        // Binary search to find potential conflicts
        const startIdx = this.findInsertionPointLogic.execute(this.newBlock.startTime);
        // Check blocks that could potentially overlap
        for (let i = startIdx; i < this.blocks.length; i++) {
            const block = this.blocks[i];
            // If we've reached a block that starts after our new block ends,
            // we can stop checking
            if (block.startTime >= this.newBlock.endTime) break;

            // Check for overlap
            if (this.newBlock.startTime < block.endTime &&
                this.newBlock.endTime > block.startTime) {
                this.conflictEventTitle = block.title;
                return true;
            }
        }
        return false;
    }

    async getBlocks(): Promise<void> {
        const event = await EventModel.findEvent(this.userId);
        this.userName = event?.userId?.name;
        this.userEmail = event?.userId?.email;
        this.availableSlots = event?.availableSlots || [];
        this.blocks = event?.blocks || [];
    }

    async checkSenderConflicts(): Promise<any> {
        const senderEmail = this.newBlock.senderEmail;
        const senderUser = await UserModel.getUserByEmail(senderEmail);
        if(senderUser) {
            this.senderUserId = senderUser._id;;
            const senderEvent = await EventModel.findEvent(senderUser._id);
            if(senderEvent) {
                this.blocks = senderEvent.blocks || [];
                if(this.hasConflict()) {
                    this.newBlock.isConflicted = true;
                    this.respWarning = `You have a conflict with: ${this.conflictEventTitle}`;
                }
            }
        }
    }
}