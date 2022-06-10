import { ProcessingType } from "./ProcessingType";

export interface ProcessingRequestReply{
    processingID: string;
    file: string;
    operation: ProcessingType;
}