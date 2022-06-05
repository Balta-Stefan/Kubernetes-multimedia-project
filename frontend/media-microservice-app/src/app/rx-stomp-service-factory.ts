import { myRxStompConfig } from "./my-rx-stomp.config";
import { StompService } from "./services/stomp.service"

export function rxStompServiceFactory(){
    const rxStomp = new StompService();
    rxStomp.configure(myRxStompConfig);
    rxStomp.activate();
    return rxStomp;
}