package pisio.backend.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController
{
    @MessageMapping("/connect")
    @SendTo("/topic/connect")
    public void handle()
    {

    }
}
