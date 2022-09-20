package signaturePdf.signaturePdfApp.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
public class ApiException {
    private final String message;
    private final HttpStatus httpStatus;
    private final ZonedDateTime timestamp;
}
