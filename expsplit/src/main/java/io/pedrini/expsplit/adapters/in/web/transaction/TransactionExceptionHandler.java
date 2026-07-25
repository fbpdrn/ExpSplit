package io.pedrini.expsplit.adapters.in.web.transaction;

import io.pedrini.expsplit.domain.transaction.exception.TransactionPermissionException;
import io.pedrini.expsplit.domain.transaction.exception.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class TransactionExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    ResponseEntity<String> handleNotFound(TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(TransactionPermissionException.class)
    ResponseEntity<String> handleNotOwner(TransactionPermissionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
