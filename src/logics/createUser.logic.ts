import { EventModel } from "../models/event.model";
import { UserModel } from "../models/user.model"

export class CreateUserLogic {

    constructor(
        protected name: string,
        protected email: string
    ) {}

    async execute() {
        try {
            await this.validate();
            const userDetails = {
                name: this.name,
                email: this.email
            }
            /** Create user in db */
            const user = await UserModel.createUser(userDetails);
            /** Create entry in events */
            if(user?._id) {
                await EventModel.addBlock(user._id, null);
            } else {
                throw {status: 500, message: "Error creating user"};
            }

        } catch(e) {
            console.log(`Error in CreateUserLogic.execute: ${e}`);
            throw e;
        }
    }

    async validate() {
        const existingUser = await UserModel.getUserByEmail(this.email);
        if(existingUser) {
            throw {status: 400, message: "User with this email already exists"};
        }
    }
}