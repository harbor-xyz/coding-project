import { ObjectId } from "mongodb";
import { UserModel } from "../models/user.model";
import { EventModel } from "../models/event.model";

export class AcceptEventLogic {

    constructor(
        protected userId: string,
        protected eventId: string,
        protected requesterEmail: string
    ) {}

    async execute() {
        try {
            await this.validate();
            await EventModel.acceptEvent(new ObjectId(this.userId), this.eventId);

        } catch(e) {
            console.log(`Error in AcceptEventLogic.execute: ${e}`);
        }
    }

    async validate() {
        try {
            const userDoc = await UserModel.getUserByEmail(this.requesterEmail);
            if(userDoc?._id.toString() !== this.userId) {
                throw {status: 403, message: "Forbidden to accept event for this user"};
            }
        } catch(e) {
            console.log(`Error in AcceptEventLogic.validate: ${e}`);
        }
    }
}