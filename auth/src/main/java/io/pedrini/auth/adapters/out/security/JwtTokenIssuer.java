package io.pedrini.auth.adapters.out.security;

import io.jsonwebtoken.Jwts;
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

    private final JwtProperties properties;
    private final KeyPair keyPair;

    JwtTokenIssuer(JwtProperties properties, KeyPair keyPair) {
        this.properties = properties;
        this.keyPair = keyPair;
    }

    @Override
    public String issue(UserAuthId userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .header().add("kid", JwtKeyConfig.KEY_ID).and()
                .subject(userId.id().toString())
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.expiration())))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }
}
