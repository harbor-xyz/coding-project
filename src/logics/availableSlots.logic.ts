import { ITimeBlock } from "../interfaces/events.interface";
import { ILogic } from "../interfaces/logic.interface";
import { EventModel } from "../models/event.model";
import { ObjectId } from "mongodb";
import { IAvailableSlot } from "./setAvailableSlots.logic";

export class GetAvailableSlotsLogic {
    blocks: ITimeBlock[];
    availableSlots: IAvailableSlot[] = [];
    constructor(
        protected start: Date,
        protected end: Date,
        protected duration: number,
        protected userId: string,
        protected findInsertionPoint: ILogic<number>
    ) {}

    async execute() {
        try {
            const availableSlots: {startTime: Date, endTime: Date}[] = [];
            const durationMs = this.duration * 60 * 1000;
            await this.getBlocks();

            /** get blocks for this user */
            if(this.availableSlots?.length) {
                return this.availableSlots;
            }
            // If there are no events, the entire range is available
            if (this.blocks.length === 0) {
                let current = new Date(this.start);
                while (current.getTime() + durationMs <= new Date(this.end).getTime()) {
                    availableSlots.push({
                        startTime: new Date(current),
                        endTime: new Date(current.getTime() + durationMs)
                    });
                    current = new Date(current.getTime() + durationMs);
                }
                return availableSlots;
            }
            // Find first relevant block
            let currentTime = new Date(this.start).getTime();
            let blockIndex = await this.findInsertionPoint.execute(this.blocks);
            // Check each gap between blocks
            while (currentTime + durationMs <= new Date(this.end).getTime()) {
                const nextBlockStart = blockIndex < this.blocks.length ?
                    this.blocks[blockIndex].startTime.getTime() :
                    new Date(this.end).getTime();

                // If there's enough time before the next block, add slots
                if (nextBlockStart - currentTime >= durationMs) {
                    availableSlots.push(
                        {
                            startTime: new Date(currentTime),
                            endTime: new Date(currentTime + durationMs)
                        });
                    currentTime += durationMs;
                } else {
                    // Skip to end of current block
                    currentTime = this.blocks[blockIndex].endTime.getTime();
                    blockIndex++;
                }
            }
            return availableSlots;
        } catch(e) {
            console.log(`Error in GetAvailableSlots.execute: ${e}`);
            throw e;
        }
    }

    async getBlocks() {
        try {
            /** get blocks for this user */
            const eventsDocForUser = await EventModel.findEvent(new ObjectId(this.userId));
            if (!eventsDocForUser) {
                throw { status: 400, message: "User not found" };
            } else {
                this.blocks = eventsDocForUser.blocks;
                this.availableSlots = eventsDocForUser.availableSlots;
            }
        } catch(e) {
            console.log(`Error in GetAvailableSlots.getBlocks: ${e}`);
            throw e;
        }
    }
}