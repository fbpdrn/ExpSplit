package io.pedrini.auth.adapters.out.security;

import io.jsonwebtoken.Jwts;
import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthId;
import io.pedrini.auth.domain.user.port.out.TokenIssuer;
import io.pedrini.auth.infrastructure.security.JwtKeyConfig;
import io.pedrini.auth.infrastructure.security.JwtProperties;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Instant;
import java.util.Date;

@Component
class JwtTokenIssuer implements TokenIssuer {

    private static final String HEADER_KEY_ID = "kid";
    private static final String CLAIM_EMAIL = "email";

    private final JwtProperties properties;
    private final KeyPair keyPair;

    JwtTokenIssuer(JwtProperties properties, KeyPair keyPair) {
        this.properties = properties;
        this.keyPair = keyPair;
    }

    @Override
    public String issue(UserAuthId userId, UserAuthEmail email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .header().add(HEADER_KEY_ID, JwtKeyConfig.KEY_ID).and()
                .subject(userId.id().toString())
                .claim(CLAIM_EMAIL, email.email())
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.expiration())))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }
}
