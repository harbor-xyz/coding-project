import { Schema, Types } from "mongoose";
import { IEvent, ITimeBlock } from "../interfaces/events.interface";
import { ObjectId } from "mongodb";
import { IAvailableSlot } from "../logics/setAvailableSlots.logic";

export const eventSchema = new Schema<IEvent>({
    userId: {
        type: Types.ObjectId,
        ref: "User",
        required: true,
    },
    timezone: {
        type: String,
        required: true,
        default: "UTC",
    },
    availableSlots: [
        {
            startTime: {
                type: Date,
                required: true,
            },
            endTime: {
                type: Date,
                required: true,
            }
        }
    ],
    blocks: [{
        startTime: {
            type: Date,
            required: true,
        },
        endTime: {
            type: Date,
            required: true,
        },
        eventType: {
            type: String,
            required: true,
        },
        eventId: {
            type: String,
            required: true,
        },
        title: {
            type: String,
        },
        description: {
            type: String,
        },
        senderEmail: {
            type: String,
            required: true,
        },
        memberEmail: {
            type: String,
        },
        isConflicted: {
            type: Boolean,
            required: true,
            default: false,
        },
        isAccepted: {
            type: Boolean,
            required: true,
            default: false,
        }
    }]
}, {timestamps: true
})

eventSchema.statics.create = async function(userId: ObjectId) {
    return this.create({userId, blocks: []});
}

eventSchema.statics.findEvent = async function(userId: string) {
    return this.findOne({userId}).populate("userId").lean();
}

eventSchema.statics.findEventsForMultipleUsers = async function(userIds: string[]) {
    return this.find({userId: {$in: userIds}});
}

eventSchema.statics.addBlock = async function(userId: ObjectId, newBlock: ITimeBlock) {
    let setQuery: any = {
        $push: {
            blocks: newBlock
        }
    }
    if(!newBlock) {
        /** set empty array */
        setQuery = {
            $set: {
                blocks: []
            }
        }
    }
    return this.updateOne(
        {
            userId
        }, setQuery, {upsert: true});
}

eventSchema.statics.setAvailableSlots = async function(userId: ObjectId, slots: IAvailableSlot[]) {
    return this.updateOne(
        {
            userId
        }, {
            $set: {
                availableSlots: slots
            }
        });
}

eventSchema.statics.acceptEvent = async function(userId: ObjectId, eventId: string) {
    return this.updateOne(
        {
            userId,
            "blocks.eventId": eventId
        }, {
            $set: {
                "blocks.$.isAccepted": true
            }
        });
}

eventSchema.statics.removeEvent = async function(eventId: string) {
    return this.updateMany(
        {
            "blocks.eventId": eventId
        }, {
            $pull: {
                blocks: {eventId}
            }
        });
}