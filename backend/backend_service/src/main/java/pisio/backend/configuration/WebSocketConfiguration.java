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
    private final TaskScheduler taskScheduler;

    public WebSocketConfiguration(@Lazy TaskScheduler taskScheduler)
    {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)
    {
        registry.addEndpoint("/ws/register").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        // destinations prefixed with /user target a message queue for the user that made that request
        registry.enableSimpleBroker("/queue")
                .setHeartbeatValue(new long[]{10000, 20000}) // server sends a heartbeat every 10 seconds while client has to send a heartbeat every 20 seconds
                .setTaskScheduler(taskScheduler);
        registry.setApplicationDestinationPrefixes("/ws"); // messages whose URL starts with this prefix are routed to the @Controller
    }
}
