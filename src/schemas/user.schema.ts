import { time } from "console";
import { Model } from "mongoose";
import { IUser } from "../interfaces/user.interface";
import mongoose from "mongoose";

// Define the schema for the collection
export const userSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true, // This field is mandatory
    },
    email: {
        type: String,
        required: true,
        unique: true, // Ensures no two users have the same email
    }
}, {timestamps: true});


userSchema.statics.createUser = async function(userDetails: IUser) {
    return this.create(userDetails);
}

userSchema.statics.getUserByEmail = async function(email: string) {
    return this.findOne({email}).lean();
}

userSchema.statics.get = async function(search: string) {
    let filter = {};

    if(search) {
        /** regex search */
        filter = {
            $or: [
                {name: {$regex: search, $options: "i"}},
                {email: {$regex: search, $options: "i"}}
            ]
        }
    }
    return this.find(filter).lean();
}
