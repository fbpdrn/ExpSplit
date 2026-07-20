package io.pedrini.auth.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

@Configuration
public class JwtKeyConfig {

    public static final String KEY_ID = "expsplit-auth-1";

    @Bean
    public KeyPair jwtSigningKeyPair(JwtProperties properties) {
        try {
            Path path = Path.of(properties.keyStorePath());
            return Files.exists(path) ? loadKeyPair(path) : generateAndStoreKeyPair(path);
        } catch (IOException | ParseException | JOSEException e) {
            throw new IllegalStateException("Unable to load or generate JWT signing key", e);
        }
    }

    private KeyPair loadKeyPair(Path path) throws IOException, ParseException, JOSEException {
        RSAKey rsaKey = RSAKey.parse(Files.readString(path));
        return new KeyPair(rsaKey.toRSAPublicKey(), rsaKey.toRSAPrivateKey());
    }

    private KeyPair generateAndStoreKeyPair(Path path) throws IOException, JOSEException {
        KeyPair keyPair = generateKeyPair();

        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyUse(KeyUse.SIGNATURE)
                .keyID(KEY_ID)
                .build();

        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.writeString(path, rsaKey.toJSONString());

        return keyPair;
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm not available", e);
        }
    }
}
