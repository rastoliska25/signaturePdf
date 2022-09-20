package signaturePdf.signaturePdfApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<Object> handleApiRequestExceptionInternalServerError(ApiRequestException e) {

        HttpStatus internalServerError = HttpStatus.NOT_FOUND;

        ApiException apiException = new ApiException(
                e.getMessage(),
                internalServerError,
                ZonedDateTime.now()
        );

        return new ResponseEntity<>(apiException, internalServerError);
    }

    @ExceptionHandler(value = {IOException.class})
    public ResponseEntity<Object> IOExceptionError (IOException e) {

        HttpStatus internalServerError = HttpStatus.BAD_REQUEST;

        ApiException apiException = new ApiException(
                e.getMessage(),
                internalServerError,
                ZonedDateTime.now()
        );

        return new ResponseEntity<>(apiException, internalServerError);
    }

    @ExceptionHandler(value = {MultipartException.class})
    public ResponseEntity<Object> MultipartExceptionError (MultipartException e) {

        HttpStatus internalServerError = HttpStatus.BAD_REQUEST;

        ApiException apiException = new ApiException(
                e.getMessage(),
                internalServerError,
                ZonedDateTime.now()
        );

        return new ResponseEntity<>(apiException, internalServerError);
    }
}
