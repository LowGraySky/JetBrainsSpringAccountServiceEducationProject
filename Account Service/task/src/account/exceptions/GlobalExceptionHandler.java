package account.exceptions;

import account.pojos.CustomErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(value = {DataIntegrityViolationException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<CustomErrorMessage> handleSQLException(Exception exception, WebRequest request){
        LOGGER.warn(exception.getMessage());
        CustomErrorMessage customErrorMessage = new CustomErrorMessage(
                HttpStatus.BAD_REQUEST,
                "Error!",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(customErrorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomErrorMessage> handleConstraintViolationException(ConstraintViolationException exception, WebRequest request){
        CustomErrorMessage errorMessage = new CustomErrorMessage(
                HttpStatus.BAD_REQUEST,
                exception.getConstraintViolations()
                        .stream()
                        .map(constraintViolation ->
                                constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage()
                        )
                        .reduce("", (acc, next) -> acc + next + ", " ),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<CustomErrorMessage> handleTransactionSystemException(Exception exception, WebRequest request){
        LOGGER.warn(exception.getMessage());
        Throwable rootCause = ((TransactionSystemException) exception).getRootCause();
        String message = "";
        if (rootCause instanceof ConstraintViolationException){
            message = ((ConstraintViolationException) rootCause)
                    .getConstraintViolations()
                    .stream()
                    .map(constraintViolation ->
                            constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage()
                    )
                    .reduce("", (acc, next) -> acc + next + ", " );
        }
        CustomErrorMessage errorMessage = new CustomErrorMessage(
                HttpStatus.BAD_REQUEST,
                message.isEmpty() ? exception.getMessage() : rootCause.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<CustomErrorMessage> handleLockedLockedException(LockedException exception, WebRequest request){
        CustomErrorMessage message = new CustomErrorMessage(
                HttpStatus.UNAUTHORIZED,
                "User account is locked",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }
}