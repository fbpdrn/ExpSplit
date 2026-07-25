package io.pedrini.expsplit.adapters.in.web.group;

import io.pedrini.expsplit.domain.group.exception.AlreadyGroupMemberException;
import io.pedrini.expsplit.domain.group.exception.GroupNotFoundException;
import io.pedrini.expsplit.domain.group.exception.NoPendingInvitationException;
import io.pedrini.expsplit.domain.group.exception.NotGroupMemberException;
import io.pedrini.expsplit.domain.group.exception.NotGroupOwnerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GroupExceptionHandler {

    @ExceptionHandler(GroupNotFoundException.class)
    ResponseEntity<String> handleNotFound(GroupNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NotGroupMemberException.class)
    ResponseEntity<String> handleNotMember(NotGroupMemberException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NoPendingInvitationException.class)
    ResponseEntity<String> handleNoPendingInvitation(NoPendingInvitationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NotGroupOwnerException.class)
    ResponseEntity<String> handleNotOwner(NotGroupOwnerException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(AlreadyGroupMemberException.class)
    ResponseEntity<String> handleAlreadyMember(AlreadyGroupMemberException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
