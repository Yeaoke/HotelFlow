package com.example.app.exceptions;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(
        {
            ReservationNotFoundException.class,
            RoomNotFoundException.class,
            UserNotFoundException.class,
            ReviewNotFoundException.class
        }
    )
    ResponseEntity<ApiError> notFound(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return error(404,e.getMessage(),r);
    }
    
    @ExceptionHandler(
        {
            ConflictException.class,
            RoomAlreadyReservedException.class,
            UserAlreadyExistsException.class
        }
    )
    ResponseEntity<ApiError> conflict(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return error(409,e.getMessage(),r);
    }
    
    @ExceptionHandler(
        DataIntegrityViolationException.class
    )
    ResponseEntity<ApiError> integrity(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return error(409,"Operation conflicts with existing data",r);
    }
    
    @ExceptionHandler(
        {
            CannotAcquireLockException.class,
            org.springframework.orm.ObjectOptimisticLockingFailureException.class
        }
    )
    ResponseEntity<ApiError> concurrency(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return error(409,"Concurrent modification; retry the request",r);
    }
    
    @ExceptionHandler(
        AuthenticationException.class
    )
    ResponseEntity<ApiError> unauth(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return error(401,"Invalid credentials or token",r);
    }
    
    @ExceptionHandler(
        AccessDeniedException.class
    )
    ResponseEntity<ApiError> denied(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return error(403,e.getMessage(),r);
    }
    
    @ExceptionHandler(
        {
            IllegalArgumentException.class,
            DaysAmountException.class,
            ConstraintViolationException.class
        }
    )
    ResponseEntity<ApiError> invalid(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return error(400,e.getMessage(),r);
    }
    
    @ExceptionHandler(
        {
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
        }
    )
    ResponseEntity<ApiError> malformed(
        Exception e,
        HttpServletRequest r
    ) {
        return error(400,"Malformed request or parameter",r);
    }
    
    @ExceptionHandler(
        MethodArgumentNotValidException.class
    )
    ResponseEntity<ApiError> validation(
        MethodArgumentNotValidException e,
        HttpServletRequest r
    ) {
        return error(400,e.getBindingResult().getFieldErrors().stream().map(x -> x.getField() + ": " + x.getDefaultMessage()).findFirst().orElse("Invalid request"), r);
    }
    
    @ExceptionHandler(
        RateLimitException.class
    )
    ResponseEntity<ApiError> limit(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return ResponseEntity.status(429).header("Retry-After", "60").body(body(429, e.getMessage(), r));
    }
    
    @ExceptionHandler(
        {
            DeliveryException.class,
            DataAccessResourceFailureException.class
        }
    )
    ResponseEntity<ApiError> unavailable(
        RuntimeException e,
        HttpServletRequest r
    ) {
        return error(503,"Service temporarily unavailable", r);
    }
    
    @ExceptionHandler(
        ResponseStatusException.class
    )
    ResponseEntity<ApiError> status(
        ResponseStatusException e,
        HttpServletRequest r
    ) {
        return error(e.getStatusCode().value(),e.getReason(), r);
    }
    private ResponseEntity<ApiError> error(
        int status,
        String message,
        HttpServletRequest r
    ) {
        return ResponseEntity.status(status).body(body(status, message, r));
    }

    private ApiError body(
        int status,
        String message,
        HttpServletRequest r
    ) {
        return new ApiError(Instant.now(), status, HttpStatus.valueOf(status).getReasonPhrase(), message, r.getRequestURI());
    }
}
