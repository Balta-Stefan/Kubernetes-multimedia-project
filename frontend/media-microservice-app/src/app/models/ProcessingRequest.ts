import { Resolution } from "./Resolution";

export interface ProcessingRequest{
    extractAudio: boolean;
    targetResolution: Resolution;
    file: string;
}