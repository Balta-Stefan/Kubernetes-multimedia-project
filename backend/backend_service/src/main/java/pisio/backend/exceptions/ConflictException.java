package pisio.backend.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends HttpException
{
    public ConflictException()
    {
        super(HttpStatus.CONFLICT);
    }

    public ConflictException(Object data)
    {
        super(HttpStatus.CONFLICT, data);
    }
}
