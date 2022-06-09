import { ProcessingProgress } from "./ProcessingProgress";
import { ProcessingType } from "./ProcessingType";

export interface Notification{
    fileName: string;
    progress: ProcessingProgress | null;
    url: string | null;
    type: ProcessingType | null;
}