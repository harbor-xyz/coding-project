import { ObjectId } from "mongodb";
import { ITimeBlock } from "../interfaces/events.interface";
import { EventModel } from "../models/event.model";

export class RemoveEventLogic  {
    blocks: ITimeBlock[];
    constructor(
        protected eventId: string,
    ) {}

    async execute() {
        try {
            await EventModel.removeEvent(this.eventId);
        } catch(e) {
            console.log(`Error in RemoveEventLogic.execute: ${e}`);
            throw e;
        }
    }
}