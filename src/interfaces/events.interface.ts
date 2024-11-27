import { ObjectId } from "mongodb";
import { EventType } from "../enums/eventType.enum";
import { IAvailableSlot } from "../logics/setAvailableSlots.logic";

export interface ITimeBlock {
    startTime: Date;
    endTime: Date;
    eventType: EventType;
    eventId: string;
    title?: string;
    description?: string;
    memberEmail?: string;
    senderEmail: string;
    isConflicted: boolean;
    isAccepted?: boolean;
}

export interface IEvent {
    userId: ObjectId | any;
    timezone: string;
    blocks: ITimeBlock[];
    availableSlots: ITimeBlock[];
}

export interface IEventModel {
    findEvent: (userId: ObjectId) => Promise<IEvent>;
    addBlock: (userId: ObjectId, blocks: ITimeBlock) => Promise<IEvent>;
    findEventsForMultipleUsers: (userIds: ObjectId[]) => Promise<IEvent[]>;
    setAvailableSlots: (userId: ObjectId, availableSlots: IAvailableSlot[]) => Promise<IEvent>;
    acceptEvent: (userId: ObjectId, eventId: string) => Promise<IEvent>;
    removeEvent: (eventId: string) => Promise<IEvent>;
}