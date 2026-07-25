package io.pedrini.expsplit.adapters.in.web.settlement;

import io.pedrini.expsplit.domain.settlement.exception.SettlementNotFoundException;
import io.pedrini.expsplit.domain.settlement.exception.SettlementPermissionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class SettlementExceptionHandler {

    @ExceptionHandler(SettlementNotFoundException.class)
    ResponseEntity<String> handleNotFound(SettlementNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SettlementPermissionException.class)
    ResponseEntity<String> handleNotOwner(SettlementPermissionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
