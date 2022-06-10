import { ProcessingProgress } from "./ProcessingProgress";
import { ProcessingType } from "./ProcessingType";

export interface Notification{
    processingID: string | null;
    fileName: string;
    progress: ProcessingProgress | null;
    url: string | null;
    type: ProcessingType | null;
}