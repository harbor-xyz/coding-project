import { ObjectId } from "mongodb";
import { EventType } from "../enums/eventType.enum";
import { ITimeBlock } from "../interfaces/events.interface";
import { EventModel } from "../models/event.model";

export class FindConflictsLogic {
    userBlocksMapping: Map<string, ITimeBlock[]>;

    constructor(
        private firstUser: string,
        private secondUser: string
    ) {
        this.userBlocksMapping = new Map<string, ITimeBlock[]>();
        this.userBlocksMapping.set(firstUser, []);
        this.userBlocksMapping.set(secondUser, []);
    }

    async execute() {
        await this.getBlocks();
        const conflicts: any[] = [];
        const overlappingEvents: any = [];
        /** check for conflicts */
        const firstUserBlocks = this.userBlocksMapping.get(this.firstUser);
        const secondUserBlocks = this.userBlocksMapping.get(this.secondUser);
        if (firstUserBlocks && secondUserBlocks) {
            for(let i=0; i<firstUserBlocks.length; i++) {
                for(let j=0; j<secondUserBlocks.length; j++) {
                    const user1Event = firstUserBlocks[i];
                    const user2Event = secondUserBlocks[j];
                    if (
                        user1Event.startTime < user2Event.endTime &&
                        user1Event.endTime > user2Event.startTime
                    ) {
                        // Calculate the actual conflict time
                        const conflictStart = new Date(
                            Math.max(user1Event.startTime.getTime(), user2Event.startTime.getTime())
                        );
                        const conflictEnd = new Date(
                            Math.min(user1Event.endTime.getTime(), user2Event.endTime.getTime())
                        );

                        conflicts.push({
                            startTime: conflictStart,
                            endTime: conflictEnd,
                            eventType: EventType.CONFLICT,
                            eventId: `conflict-${user1Event.eventId}-${user2Event.eventId}`,
                        });

                        overlappingEvents.push({
                            firstEvent: user1Event,
                            secondEvent: user2Event,
                            conflict: {
                                startTime: conflictStart,
                                endTime: conflictEnd,
                            }
                        });

                    }
                }
            }
        }
        return {conflicts, overlappingEvents};
    }

    async getBlocks() {
        const userEvents = await EventModel.findEventsForMultipleUsers([new ObjectId(this.firstUser), new ObjectId(this.secondUser)]);
        userEvents.forEach(event => {
            this.userBlocksMapping.set(event.userId.toString(), event.blocks ?? []);
        });
    }
}