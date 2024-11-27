import { ITimeBlock } from "../interfaces/events.interface";
import { ILogic } from "../interfaces/logic.interface";

export class FindInsertionPointLogic implements ILogic<any> {

    constructor(
        protected time: Date
    ) {}

    execute(blocks: any[]): number {
        try {
            let low = 0;
            let high = blocks.length;

            while (low < high) {
                const mid = Math.floor((low + high) / 2);
                if (blocks[mid].startTime < this.time) {
                    low = mid + 1;
                } else {
                    high = mid;
                }
            }
            return low;
        } catch(e) {
            console.log(`Error in FindInsertionPointLogic.execute: ${e}`);
            throw e;
        }
    }
}
