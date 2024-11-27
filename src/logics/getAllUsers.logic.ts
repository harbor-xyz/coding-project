import { UserModel } from "../models/user.model";

export class GetAllUsersLogic {

    constructor(protected search: string) {}

    async execute() {
        try {
            const users = await UserModel.get(this.search);
            return users?.length ? users : [];
        } catch(e) {
            console.log(`Error in GetAllUsersLogic.execute: ${e}`);
        }
    }
}