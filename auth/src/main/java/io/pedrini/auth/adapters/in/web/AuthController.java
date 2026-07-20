package io.pedrini.auth.adapters.in.web;

import io.pedrini.auth.adapters.in.web.dto.LoginRequest;
import io.pedrini.auth.adapters.in.web.dto.LoginResponse;
import io.pedrini.auth.adapters.in.web.dto.RegisterRequest;
import io.pedrini.auth.adapters.in.web.dto.RegisterResponse;
import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthId;
import io.pedrini.auth.domain.user.port.in.AuthenticateUserUseCase;
import io.pedrini.auth.domain.user.port.in.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserAuthId id = registerUserUseCase.register(new UserAuthEmail(request.email()), request.password());
        return ResponseEntity.ok(new RegisterResponse(id.id()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authenticateUserUseCase.authenticate(new UserAuthEmail(request.email()), request.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
