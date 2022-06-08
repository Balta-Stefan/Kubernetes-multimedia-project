import { ProcessingProgress } from "./ProcessingProgress";

export interface Notification{
    fileName: string;
    progress: ProcessingProgress | null;
    url: string | null;
}