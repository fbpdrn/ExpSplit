package io.pedrini.expsplit.adapters.in.web.transaction;

import io.pedrini.expsplit.adapters.in.web.transaction.dto.CreateTransactionRequest;
import io.pedrini.expsplit.adapters.in.web.transaction.dto.TransactionResponse;
import io.pedrini.expsplit.adapters.in.web.transaction.dto.TransactionShareRequest;
import io.pedrini.expsplit.adapters.in.web.transaction.dto.UpdateTransactionRequest;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.transaction.model.SharePercentage;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.transaction.model.TransactionDescription;
import io.pedrini.expsplit.domain.transaction.model.TransactionId;
import io.pedrini.expsplit.domain.transaction.model.TransactionShare;
import io.pedrini.expsplit.domain.transaction.port.in.CreateTransactionUseCase;
import io.pedrini.expsplit.domain.transaction.port.in.DeleteTransactionUseCase;
import io.pedrini.expsplit.domain.transaction.port.in.GetTransactionUseCase;
import io.pedrini.expsplit.domain.transaction.port.in.ListTransactionsUseCase;
import io.pedrini.expsplit.domain.transaction.port.in.UpdateTransactionUseCase;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/groups/{groupId}/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                  GetTransactionUseCase getTransactionUseCase,
                                  ListTransactionsUseCase listTransactionsUseCase,
                                  UpdateTransactionUseCase updateTransactionUseCase,
                                  DeleteTransactionUseCase deleteTransactionUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.getTransactionUseCase = getTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.updateTransactionUseCase = updateTransactionUseCase;
        this.deleteTransactionUseCase = deleteTransactionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId,
                                                        @Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = createTransactionUseCase.create(
                new GroupId(groupId), userId(jwt),
                new TransactionDescription(request.description()), new Amount(request.amount()), request.category(), shares(request.shares()));
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        List<TransactionResponse> transactions = listTransactionsUseCase.list(new GroupId(groupId), userId(jwt)).stream()
                .map(TransactionResponse::from)
                .toList();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId,
                                                     @PathVariable UUID transactionId) {
        Transaction transaction = getTransactionUseCase.get(new GroupId(groupId), new TransactionId(transactionId), userId(jwt));
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId,
                                                        @PathVariable UUID transactionId,
                                                        @Valid @RequestBody UpdateTransactionRequest request) {
        Transaction transaction = updateTransactionUseCase.update(
                new GroupId(groupId), new TransactionId(transactionId), userId(jwt),
                new TransactionDescription(request.description()), new Amount(request.amount()), request.category(), shares(request.shares()));
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId,
                                        @PathVariable UUID transactionId) {
        deleteTransactionUseCase.delete(new GroupId(groupId), new TransactionId(transactionId), userId(jwt));
        return ResponseEntity.noContent().build();
    }

    private List<TransactionShare> shares(List<TransactionShareRequest> shares) {
        return shares.stream()
                .map(s -> new TransactionShare(new UserProfileId(s.userId()), new SharePercentage(s.percentage())))
                .toList();
    }

    private UserProfileId userId(Jwt jwt) {
        return new UserProfileId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
    }
}
