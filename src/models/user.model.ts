import { model } from "mongoose";
import { IUserModel } from "../interfaces/user.interface";
import { userSchema } from "../schemas/user.schema";

export const UserModel: IUserModel = model<IUserModel, IUserModel>("User", userSchema);