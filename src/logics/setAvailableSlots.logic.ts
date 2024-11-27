import { ObjectId } from 'mongodb';
import { EventModel } from '../models/event.model';
import { ITimeBlock } from '../interfaces/events.interface';

export interface IAvailableSlot {
    startTime: Date;
    endTime: Date;
}

export class SetAvailableSlots {
    blocks: ITimeBlock[];
    constructor(
        protected availableSlots: IAvailableSlot[],
        protected userId: ObjectId,
        protected email: string
    ) {}

    async execute() {
        try {
            this.processSlots();
            await this.getBlocks();
            /** check if any available slot is conflicting with blocked slots */
            const failedToSetAvailableSlots = [];
            const processedAvailableSlots = [];
            for(let i=0; i<this.availableSlots.length; i++) {
                const slot = this.availableSlots[i];
                let conflictFlag = false;
                for(let j=0; j<this.blocks.length; j++) {
                    const block = this.blocks[j];
                    if (
                        slot.startTime < block.endTime &&
                        slot.endTime > block.startTime
                    ) {
                        conflictFlag = true;
                        failedToSetAvailableSlots.push(slot);
                        break;
                    }
                }
                if (!conflictFlag) {
                    processedAvailableSlots.push(slot);
                }
            }
            await EventModel.setAvailableSlots(this.userId, processedAvailableSlots);
            return  {
                failedToSetAvailableSlots,
                processedAvailableSlots
            }
        } catch(e) {
            console.log(`Error in SetAvailableSlots.execute: ${e}`);
            throw e;
        }
    }

    processSlots() {
        for(const slot of this.availableSlots) {
            slot.startTime = new Date(slot.startTime);
            slot.endTime = new Date(slot.endTime);
        }
    }

    async getBlocks() {
        try {
            const eventsDocForUser = await EventModel.findEvent(new ObjectId(this.userId));
            if(eventsDocForUser.userId?.email !== this.email) {throw {status: 403, message: "Forbidden to set slots for this user"}};
            this.blocks = eventsDocForUser?.blocks || [];
        } catch(e) {
            console.log(`Error in SetAvailableSlots.getBlocks: ${e}`);
            throw e;
        }
    }
}