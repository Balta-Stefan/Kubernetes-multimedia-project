import { ProcessingProgress } from "./ProcessingProgress";

export interface Notification{
    fileName: string;
    progress: ProcessingProgress;
    url: string | null;
}