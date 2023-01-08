package se.magnus.microservices.composite.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.microservices.composite.product.services.ProductCompositeIntegration;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {ProductCompositeServiceApplication.class, TestSecurityConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true","eureka.client.enabled=false","spring.cloud.config.enabled=false"})
public class ProductCompositeServiceApplicationTests {
    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductCompositeIntegration productCompositeIntegration;

    @BeforeEach
    public void setUp(){
        Mockito.when(productCompositeIntegration.getProduct(eq(PRODUCT_ID_OK), anyInt(), anyInt()))
                .thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));

        Mockito.when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(Flux.fromIterable(Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address"))));

        Mockito.when(productCompositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(Flux.fromIterable(Collections.singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));

        Mockito.when(productCompositeIntegration.getProduct(eq(PRODUCT_ID_NOT_FOUND), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

        Mockito.when(productCompositeIntegration.getProduct(eq(PRODUCT_ID_INVALID), anyInt(), anyInt()))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }


    @Test
    public void getProductById(){
        getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
            .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("$.recommendations.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    public void getProductNotFound(){
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
            .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    public void getProductInvalidInput(){
        getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
            .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus){
        return webTestClient.get()
                .uri("/product-composite/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }
}
