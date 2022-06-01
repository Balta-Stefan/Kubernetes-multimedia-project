package pisio.backend.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HttpException
{
    public UnauthorizedException()
    {
        super(HttpStatus.UNAUTHORIZED);
    }


    public UnauthorizedException(Object data)
    {
        super(HttpStatus.UNAUTHORIZED, data);
    }
}
