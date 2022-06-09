import { Notification } from "./Notification";

export interface ProcessingItem{
    file: string;
    notifications: Notification[];
}