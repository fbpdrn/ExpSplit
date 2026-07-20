package io.pedrini.gateway.proxy;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.upstream")
public record GatewayProperties(String authUrl) {
}
