package pisio.common.model.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseMessage
{
    protected String username; // username is needed when sending a message to the user's queue
    protected String bucket;
    protected String prefix;
    protected String file;
}
