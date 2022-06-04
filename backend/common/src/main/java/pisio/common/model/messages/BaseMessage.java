package pisio.common.model.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseMessage
{
    private String username; // username is needed when sending a message to the user's queue
    private String bucket;
    private String object;
}
