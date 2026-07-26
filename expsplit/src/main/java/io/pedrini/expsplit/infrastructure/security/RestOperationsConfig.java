package io.pedrini.expsplit.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;

@Configuration
class RestOperationsConfig {

    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String TLS_PROTOCOL = "TLS";

    @Bean
    @Profile("!nomtls")
    RestTemplate mtlsRestOperations(MtlsProperties properties) throws Exception {
        requireConfigured(properties);

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        try (FileInputStream in = new FileInputStream(properties.keystorePath())) {
            keyStore.load(in, properties.keystorePassword().toCharArray());
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, properties.keystorePassword().toCharArray());

        KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
        try (FileInputStream in = new FileInputStream(properties.truststorePath())) {
            trustStore.load(in, properties.truststorePassword().toCharArray());
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance(TLS_PROTOCOL);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        HttpClient httpClient = HttpClient.newBuilder().sslContext(sslContext).build();
        return new RestTemplate(new JdkClientHttpRequestFactory(httpClient));
    }

    @Bean
    @Profile("nomtls")
    RestTemplate plainRestOperations() {
        return new RestTemplate();
    }

    private static void requireConfigured(MtlsProperties properties) {
        if (properties.keystorePath() == null || properties.keystorePassword() == null
                || properties.truststorePath() == null || properties.truststorePassword() == null) {
            throw new IllegalStateException("mTLS enabled but no files/passwords set.");
        }
    }
}
