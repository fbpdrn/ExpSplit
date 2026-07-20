package io.pedrini.gateway.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

@RestController
class AuthProxyController {

    private static final Set<String> EXCLUDED_REQUEST_HEADERS = Set.of("host", "content-length", "connection");
    private static final Set<String> EXCLUDED_RESPONSE_HEADERS = Set.of("transfer-encoding", "content-length", "connection");

    private final RestClient restClient = RestClient.create();
    private final GatewayProperties properties;

    AuthProxyController(GatewayProperties properties) {
        this.properties = properties;
    }

    @RequestMapping("/auth/**")
    ResponseEntity<byte[]> forward(HttpServletRequest request, @RequestBody(required = false) byte[] body) {

        // Si crea l'url con i parametri della query string se presenti
        String queryString = request.getQueryString();
        String targetUrl = properties.authUrl() + request.getRequestURI() + (queryString != null ? "?" + queryString : "");

        // Si preparano gli headers da inoltrare, rimuovendo quelli che non devono essere inoltrati:
        // * host: header hop-by-hop riferito al gateway
        // * content-length: viene ricalcolato utilizzando body()
        // * connection: header hop-by-hop (valido solo tra client <-> gateway)
        HttpHeaders forwardedHeaders = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!EXCLUDED_REQUEST_HEADERS.contains(name.toLowerCase())) {
                forwardedHeaders.addAll(name, Collections.list(request.getHeaders(name)));
            }
        }

        return restClient.method(HttpMethod.valueOf(request.getMethod()))
                .uri(targetUrl)
                .headers(headers -> headers.addAll(forwardedHeaders))
                .body(body != null ? body : new byte[0])
                .exchange((_, clientResponse) -> {

                    // Si preparano gli headers della risposta, rimuovendo quelli che non devono essere inoltrati:
                    // * transfer-encoding: header hop-by-hop
                    // * content-length: viene ricalcolato utilizzando body()
                    // * connection: header hop-by-hop (valido solo tra gateway <-> servizio)
                    HttpHeaders responseHeaders = new HttpHeaders();
                    clientResponse.getHeaders().forEach((name, values) -> {
                        if (!EXCLUDED_RESPONSE_HEADERS.contains(name.toLowerCase())) {
                            responseHeaders.addAll(name, values);
                        }
                    });

                    return ResponseEntity
                            .status(clientResponse.getStatusCode())
                            .headers(responseHeaders)
                            .body(clientResponse.getBody().readAllBytes());
                });
    }
}
