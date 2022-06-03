package pisio.backend.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerError extends HttpException
{
    public InternalServerError()
    {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerError(Object data)
    {
        super(HttpStatus.INTERNAL_SERVER_ERROR, data);
    }
}
