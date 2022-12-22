package se.magnus.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;

import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@EnableBinding(ProductCompositeIntegration.MessageSources.class)
@Component
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private final ObjectMapper objectMapper;
    private final String productServiceUrl = "http://product";
    private final String recommendationServiceUrl = "http://recommendation";
    private final String reviewServiceUrl = "http://review";

    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private MessageSources messageSources;

    public ProductCompositeIntegration(
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            MessageSources messageSources) {
        this.objectMapper = objectMapper;
        this.webClientBuilder = webClientBuilder;
        this.messageSources = messageSources;
    }

    public interface MessageSources {
        String OUTPUT_PRODUCTS = "output-products";
        String OUTPUT_RECOMMENDATIONS = "output-recommendations";
        String OUTPUT_REVIEWS = "output-reviews";

        @Output(OUTPUT_PRODUCTS)
        MessageChannel outputProducts();
        @Output(OUTPUT_RECOMMENDATIONS)
        MessageChannel outputRecommendations();
        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = productServiceUrl + "/product/" + productId;
        log.debug("Will call the getProduct API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Product createProduct(Product body) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public void deleteProduct(int productId) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    private String getErrorMessage(WebClientResponseException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch (IOException ioException){
            return ioException.getMessage();
        }
    }


    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        log.debug("Will call getRecommendations API on URL: {}", url);
        return getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log()
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public void deleteRecommendations(int productId) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        String url = reviewServiceUrl + "/review?productId=" + productId;
        log.debug("Will call getReviews API on URL: {}", url);

        return getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log()
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Review createReview(Review body) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public void deleteReviews(int productId) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    private WebClient getWebClient(){
        if(webClient == null)  {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }

    public Mono<Health> getProductHealth(){
        return getHealth(productServiceUrl);
    }

    public Mono<Health> getRecommendationHealth(){
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewHealth(){
        return getHealth(reviewServiceUrl);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        log.debug("Will call the Health API on URL: {}", url);

        return getWebClient().get()
                .uri(url)
                .retrieve()
                .bodyToMono(Health.class)
                .map(h -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }

    private Throwable handleException(Throwable ex){
        if(!(ex instanceof  WebClientResponseException)){
            log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException exception = (WebClientResponseException) ex;

        switch (exception.getStatusCode()){
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(exception));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(exception));
            default:
                log.warn("Got a unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
                log.warn("Error body: {}", exception.getResponseBodyAsString());
                return exception;
        }
    }

}
