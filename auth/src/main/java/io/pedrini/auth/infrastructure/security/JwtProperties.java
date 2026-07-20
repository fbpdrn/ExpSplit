package io.pedrini.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String issuer, Duration expiration, String keyStorePath) {
}
