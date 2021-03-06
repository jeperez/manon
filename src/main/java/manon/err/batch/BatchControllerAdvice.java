package manon.err.batch;

import manon.err.AbstractControllerAdvice;
import manon.err.AbstractManagedException;
import manon.err.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class BatchControllerAdvice implements AbstractControllerAdvice {

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNotFound(AbstractManagedException error) {
        return error(error);
    }
}
