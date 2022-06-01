package pisio.backend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer
{
    private final TaskScheduler messageBrokerScheduler;

    public WebSocketConfiguration(@Lazy TaskScheduler messageBrokerScheduler)
    {
        this.messageBrokerScheduler = messageBrokerScheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)
    {
        registry.addEndpoint("/notifications").withSockJS(); // the endpoint is /ws/notifications
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        // destinations prefixed with /user target a message queue for the user that made that request
        registry.enableSimpleBroker("/secured/user/queue/specific-user")
                .setHeartbeatValue(new long[]{10000, 20000}) // server sends a heartbeat every 10 seconds while client has to send a heartbeat every 20 seconds
                .setTaskScheduler(this.messageBrokerScheduler); // documentation is vague.If a message contains this URI, it is probably sent directly to the in memory broker and not to the @Controller
        registry.setApplicationDestinationPrefixes("/ws"); // messages whose URL starts with this prefix are routed to the @Controller
        registry.setUserDestinationPrefix("/secured/user");
    }
}
