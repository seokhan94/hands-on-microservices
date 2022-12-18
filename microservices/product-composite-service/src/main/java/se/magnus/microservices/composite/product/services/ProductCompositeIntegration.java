package se.magnus.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        this.reviewServiceUrl = "http://" + reviewServiceHost+ ":" + reviewServicePort + "/review?productId=";
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            log.debug("Will call getProduct API on URL: {}", url);

            Product product = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Product>() {}
            ).getBody();

            log.debug("Found a product with id: {}", productId);

            return product;
        }catch (HttpClientErrorException e){
            throw handleHttpClientException(e);
        }
    }

    @Override
    public Product createProduct(Product body) {
        try {
            String url = productServiceUrl;
            log.debug("Will post a new product to URL: {}", url);

            HttpEntity<Product> httpEntity = new HttpEntity<>(body, null);

            Product product = restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<Product>() {}).getBody();
            log.debug("Created a product with id: {}", product.getProductId());

            return product;
        }catch (HttpClientErrorException e){
            throw handleHttpClientException(e);
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;

            log.debug("Will call the deleteProduct API on URL: {}", url);

            restTemplate.delete(url);
        }catch (HttpClientErrorException e){
            throw handleHttpClientException(e);
        }
    }

    private String getErrorMessage(HttpClientErrorException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch (IOException ioException){
            return ioException.getMessage();
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;

            log.debug("Will call getRecommendations API on URL: {}", url);

            List<Recommendation> recommendations = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Recommendation>>() {}
            ).getBody();

            log.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);

            return recommendations;
        }catch (Exception e){
            log.warn("Got an exception while requesting recommendations, return zero recommendations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try{
            String url = recommendationServiceUrl;
            log.debug("Will post a new recommendation to URL: {}", url);

            HttpEntity<Recommendation> httpEntity = new HttpEntity<>(body, null);
            Recommendation recommendation = restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<Recommendation>() {}).getBody();
            log.debug("Created a recommendation with id: {}", recommendation.getProductId());

            return recommendation;
        }catch (HttpClientErrorException e){
            throw handleHttpClientException(e);
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;
            log.debug("Will call the deleteRecommendations API on URL: {}", url);

            restTemplate.delete(url);
        }catch (HttpClientErrorException e){
            throw handleHttpClientException(e);
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;

            log.debug("Will call getReviews API on URL: {}", url);

            List<Review> reviews = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Review>>() {}
            ).getBody();

            log.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);

            return reviews;
        }catch (Exception e){
            log.warn("Got an exception while requesting reviews, return zero reviews: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException e) {
        switch (e.getStatusCode()){
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(e));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(e));
            default:
                log.warn("Got a unexpected HTTP error: {}, will rethrow it", e.getStatusCode());
                log.warn("Error body: {}", e.getResponseBodyAsString());
                return e;
        }
    }
}
