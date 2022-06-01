package pisio.backend.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends HttpException
{
    public ForbiddenException()
    {
        super(HttpStatus.FORBIDDEN);
    }


    public ForbiddenException(Object data)
    {
        super(HttpStatus.FORBIDDEN, data);
    }
}
