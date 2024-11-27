import { ObjectId } from "mongodb";
import { ITimeBlock } from "../interfaces/events.interface";
import { EventModel } from "../models/event.model";

export class GetEventsLogic {
    blocks: ITimeBlock[];
    constructor(
        protected startDate: Date,
        protected endDate: Date,
        protected userId: string,
        protected findInsertionPointLogic: any
    ) {}

    async execute(): Promise<ITimeBlock[]> {
        await this.getBlocks();
        const startIdx = this.findInsertionPointLogic.execute(this.blocks);
        const results: ITimeBlock[] = [];
        for (let i = startIdx; i < this.blocks.length; i++) {
            const block = this.blocks[i];
            if (block.startTime >= this.endDate) break;
            if (block.endTime > this.startDate) {
                results.push(block);
            }
        }

        return results;
    }

    async getBlocks() {
        const event = await EventModel.findEvent(new ObjectId(this.userId));
        this.blocks = event?.blocks || [];
    }
}