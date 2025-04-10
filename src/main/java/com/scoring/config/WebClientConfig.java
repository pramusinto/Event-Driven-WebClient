package com.scoring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
@Log4j2
public class WebClientConfig {

    @Value("${app.timeout}")
    private Long timeout;

//    @Value(value = "${app.scoring-url}")
//    private String urlScoring;

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public WebClient.Builder webClientBuilder() throws SSLException {

            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create()
                    .responseTimeout(java.time.Duration.ofSeconds(timeout))
                    .secure(t -> t.sslContext(sslContext));

            return WebClient.builder()
//                    .baseUrl(urlScoring)
                    .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
