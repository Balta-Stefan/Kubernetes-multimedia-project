import { ProcessingProgress } from "./ProcessingProgress";

export interface ProcessingItem{
    itemID: number;
    uploadTimestamp: Date;
    progress: ProcessingProgress;
    fileName: string;
    url: string;
}