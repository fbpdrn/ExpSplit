package io.pedrini.expsplit.adapters.in.web.user;

import io.pedrini.expsplit.domain.user.exception.UserProfileAlreadyExistsException;
import io.pedrini.expsplit.domain.user.exception.UserProfileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class UserProfileExceptionHandler {

    @ExceptionHandler(UserProfileNotFoundException.class)
    ResponseEntity<String> handleNotFound(UserProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserProfileAlreadyExistsException.class)
    ResponseEntity<String> handleAlreadyExists(UserProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<String> handleInvalidInput(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
