package io.pedrini.auth.infrastructure.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
class JwksController {

    private final RSAKey rsaKey;

    JwksController(KeyPair keyPair) {
        this.rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyID(JwtKeyConfig.KEY_ID)
                .build();
    }

    @GetMapping("/.well-known/jwks.json")
    Map<String, Object> jwks() {
        return new JWKSet(rsaKey).toJSONObject();
    }
}
