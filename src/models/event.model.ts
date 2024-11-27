import { model } from "mongoose";
import { IEvent, IEventModel } from "../interfaces/events.interface";
import { eventSchema } from "../schemas/event.schema";

export const EventModel: IEventModel = model<IEvent, IEventModel>("event", eventSchema);