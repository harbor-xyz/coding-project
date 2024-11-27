import { ObjectId } from "mongodb";

export interface IUser {
    _id?: ObjectId;
    name: String;
    email: String;
}


export interface IUserModel {
    createUser(userDetails: IUser): Promise<IUser>;
    getUserByEmail(email: string): Promise<IUser>;
    get(search: string): Promise<IUser[]>;
}