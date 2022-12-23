package se.magnus.springcloud.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Configuration
public class HealthCheckConfiguration {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckConfiguration.class);
    private final StatusAggregator statusAggregator;
    private final WebClient.Builder webClientBuilder;

    private WebClient webClient;

    public HealthCheckConfiguration(WebClient.Builder webClientBuilder, StatusAggregator statusAggregator) {
        this.webClientBuilder = webClientBuilder;
        this.statusAggregator = statusAggregator;
    }

    private Mono<Health> getHealth(String url){
        url += "/actuator/health";
        log.debug("Will call the Health API on URL: {}", url);
        return getWebClient().get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }
}
