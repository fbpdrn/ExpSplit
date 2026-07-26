package io.pedrini.gateway.mtls;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mtls")
public record MtlsProperties(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword) {
}
