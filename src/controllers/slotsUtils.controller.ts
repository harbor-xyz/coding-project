import { ITimeBlock } from "../interfaces/events.interface";
import { AddEventLogic } from "../logics/addEvent.logic";
import { GetAvailableSlotsLogic } from "../logics/availableSlots.logic";
import { FindInsertionPointLogic } from "../logics/findInsertionPoint.logic";
import { v4 as uuidv4 } from 'uuid';
import { GetEventsLogic } from "../logics/getEvents.logic";
import { RemoveEventLogic } from "../logics/removeEvent.logic";
import { FindConflictsLogic } from "../logics/findConflicts.logic";
import { SetAvailableSlots } from "../logics/setAvailableSlots.logic";
import { AcceptEventLogic } from "../logics/acceptEvent.logic";

export class SlotsUtilSController {

    static async getAvailableSlots(req, res) {
        const responseObj = {
            message: "Successfully retrieved available slots",
            data: [],
            meta: {
                success: true
            }
        }
        try {
            const { startDate, endDate } = req.query;
            const { userId } = req.params;

            if(!startDate || !endDate) {
                throw { status: 400, message: "Please provide a start and end date" };
            }

            if(!userId) {
                throw { status: 400, message: "Please provide a user id" };
            }

            const duration = req.body.duration || 30; /** default to 30 */
            const findInsertionPointLogic = new FindInsertionPointLogic(startDate);
            const getAvailableSlotsLogic = new GetAvailableSlotsLogic(startDate, endDate, duration, userId, findInsertionPointLogic);
            const slots = await getAvailableSlotsLogic.execute();
            responseObj.data = slots as any;
            res.status(200).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }

    static async bookSlot(req, res) {
        const responseObj = {
            message: "Successfully booked slot",
            data: {},
            meta: {
                success: true
            }
        }
        try {
            const { startTime, endTime, eventType, title, description } = req.body;
            const { userId } = req.params;
            const { email: senderEmail } = req.headers;
            if(!startTime || !endTime || !eventType || !title) {
                throw { status: 400, message: "Please provide a start time, end time, event type, and title" };
            }
            if(!senderEmail) {
                throw { status: 400, message: "Please provide a sender email" };
            }
            const eventId = uuidv4();
            const event: ITimeBlock = {
                startTime: new Date(startTime),
                endTime: new Date(endTime),
                eventType,
                eventId,
                title,
                description,
                senderEmail,
                isConflicted: false
            }
            const findInsertionPointLogic = new FindInsertionPointLogic(startTime);
            const addEventLogic = new AddEventLogic(event, findInsertionPointLogic, userId);
            const warning = await addEventLogic.execute();
            responseObj.data = event;
            responseObj.message = warning.length > 0 ? warning : responseObj.message;
            res.status(200).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }

    static async getEvents(req, res) {
        const responseObj = {
            message: "Successfully retrieved events",
            data: [],
            meta: {
                success: true
            }
        }
        try {
            const { startDate, endDate } = req.query;
            const { userId } = req.params;
            const findInsertionPointLogic = new FindInsertionPointLogic(new Date(startDate));
            const getEventsLogic = new GetEventsLogic(new Date(startDate), new Date(endDate), userId, findInsertionPointLogic);
            const events = await getEventsLogic.execute();
            responseObj.data = events as any;
            res.status(200).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }

    static async cancelEvent(req, res) {
        const responseObj = {
            message: "Successfully cancelled event",
            data: {},
            meta: {
                success: true
            }
        }
        try {
            const { eventId } = req.params;
            const removeEventLogic = new RemoveEventLogic(eventId);
            await removeEventLogic.execute();
            res.status(200).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }

    static async getConflicts(req, res) {
        const responseObj = {
            message: "Successfully retrieved conflicts",
            data: {},
            meta: {
                success: true
            }
        }
        try {
            const { firstUserId, secondUserId } = req.query;
            const findConflictsLogic = new FindConflictsLogic(firstUserId, secondUserId);
            const { conflicts, overlappingEvents } = await findConflictsLogic.execute();
            responseObj.data = { conflicts, overlappingEvents }
            res.status(200).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }

    static async setAvailableSlots(req, res) {
        const responseObj = {
            message: "Successfully set available slots",
            data: {},
            meta: {
                success: true
            }
        }
        try {
            const { availableSlots } = req.body;
            const { userId } = req.params;
            const {email} = req.headers;
            if(!userId || !availableSlots) {
                throw { status: 400, message: "Please provide a user id and available slots" };
            }
            const setAvailableSlotsLogic = new SetAvailableSlots(availableSlots, userId, email);
            const { failedToSetAvailableSlots, processedAvailableSlots } = await setAvailableSlotsLogic.execute();
            responseObj.data = { failedToSetAvailableSlots, processedAvailableSlots };
            responseObj.message = failedToSetAvailableSlots.length > 0 ? "Some slots could not be set as available" : responseObj.message;
            res.status(201).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }

    static async acceptEvent(req, res) {
        const responseObj = {
            message: "Successfully accepted event",
            data: {},
            meta: {
                success: true
            }
        }
        try {
            const { userId, eventId } = req.params;
            const { email } = req.headers;
            if(!userId || !eventId) {
                throw { status: 400, message: "Please provide a user id and event id" };
            }
            const acceptEventLogic = new AcceptEventLogic(userId, eventId, email);
            await acceptEventLogic.execute();
            res.status(201).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }
}