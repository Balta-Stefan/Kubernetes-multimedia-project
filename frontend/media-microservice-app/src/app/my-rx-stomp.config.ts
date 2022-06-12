import { RxStompConfig } from "@stomp/rx-stomp";

export const myRxStompConfig: RxStompConfig = {
    // Which server?
    brokerURL: "ws://localhost:80/ws/register",
  
    // How often to heartbeat?
    // Interval in milliseconds, set to 0 to disable
    heartbeatIncoming: 0, // Typical value 0 - disabled
    heartbeatOutgoing: 15000,
  
    // Wait in milliseconds before attempting auto reconnect
    // Set to 0 to disable
    // Typical value 500 (500 milli seconds)
    reconnectDelay: 200,
  
    // Will log diagnostics on console
    // It can be quite verbose, not recommended in production
    // Skip this key to stop logging to console
    debug: (msg: string): void => {
      console.log(new Date(), msg);
    },
  };