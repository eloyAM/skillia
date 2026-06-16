package io.skillia.restcontroller.config;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestGlobalExceptionHandler {

    private static final String MESSAGE_FIELD = "message";
    private static final String VALIDATION_ERROR = "Validation error";

    private RestGlobalExceptionHandler() {
    }

    // @Hidden -> avoid showing by default the handled @ResponseStatus on every OpenAPI doc endpoint
    @Hidden
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException e) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(VALIDATION_ERROR);
        problem.setDetail(e.getLocalizedMessage());
        problem.setProperty(MESSAGE_FIELD, e.getLocalizedMessage());
        return ResponseEntity.badRequest().body(problem);
    }

    @Hidden
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public static ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        final ProblemDetail problem = ex.getBody();
        Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
            .filter(FieldError.class::isInstance)
            .collect(Collectors.toMap(
                error -> ((FieldError) error).getField(),
                error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Error")
            ));
        final String briefMessage = errors.entrySet().stream()
            .findFirst()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .orElse(VALIDATION_ERROR);
        problem.setTitle(VALIDATION_ERROR);
        problem.setProperty(MESSAGE_FIELD, briefMessage);
        problem.setProperty("errors", List.of(errors));
        return problem;
    }

    @Hidden
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex
    ) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setDetail(ex.getMessage());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public static ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException ex) {
        var problem = ProblemDetail.forStatus(ex.getStatusCode());
        problem.setTitle("Error occurred");
        problem.setDetail(ex.getReason());
        problem.setProperty(MESSAGE_FIELD, ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(problem);
    }

    @Hidden
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public static ResponseEntity<ProblemDetail> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        List<String> supportedMethodsStrList = Objects.requireNonNullElse(ex.getSupportedHttpMethods(), List.of())
            .stream().map(Objects::toString).toList();
        var problem = ProblemDetail.forStatus(HttpStatus.METHOD_NOT_ALLOWED);
        problem.setTitle("Method Not Allowed");
        problem.setDetail("The HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.");
        problem.setProperty("supportedMethods", supportedMethodsStrList);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problem);
    }
}
