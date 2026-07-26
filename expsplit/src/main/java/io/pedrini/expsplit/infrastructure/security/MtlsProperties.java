package io.pedrini.expsplit.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mtls")
public record MtlsProperties(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword) {
}
