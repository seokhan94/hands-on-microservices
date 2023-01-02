package se.magnus.microservices.composite.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.api.event.Event;
import se.magnus.microservices.composite.product.services.ProductCompositeIntegration;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static se.magnus.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {ProductCompositeServiceApplication.class, TestSecurityConfig.class},
        properties = {"eureka.client.enabled=false", "spring.main.allow-bean-definition-overriding=true"})
public class MessagingTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductCompositeIntegration.MessageSources channels;

    @Autowired
    private MessageCollector collector;

    BlockingQueue<Message<?>> queueProducts = null;
    BlockingQueue<Message<?>> queueRecommendations = null;
    BlockingQueue<Message<?>> queueReviews = null;

    @BeforeEach
    public void setUp(){
        queueProducts = getQueue(channels.outputProducts());
        queueRecommendations = getQueue(channels.outputRecommendations());
        queueReviews = getQueue(channels.outputReviews());
    }

    @Test
    public void createCompositeProduct1(){
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);
        postAndVerifyProduct(compositeProduct, HttpStatus.OK);

        assertEquals(1, queueProducts.size());
        Event<Integer, Product> expectedEvent = new Event(Event.Type.CREATE, compositeProduct.getProductId(), new Product(compositeProduct.getProductId(), compositeProduct.getName(), compositeProduct.getWeight(), null));
        assertThat(queueProducts, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

        assertEquals(0, queueReviews.size());
        assertEquals(0, queueRecommendations.size());

    }

    @Test
    public void createCompositeProduct2(){
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
                Collections.singletonList(new RecommendationSummary(1, "1", 1, "c")),
                Collections.singletonList(new ReviewSummary(1, "a", "s", "c")), null);

        postAndVerifyProduct(compositeProduct, HttpStatus.OK);

        assertEquals(1, queueProducts.size());
        Event<Integer, Product> expectedEvent = new Event(Event.Type.CREATE, compositeProduct.getProductId(), new Product(compositeProduct.getProductId(), compositeProduct.getName(), compositeProduct.getWeight(), null));
        assertThat(queueProducts, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));


        assertEquals(1, queueRecommendations.size());
        RecommendationSummary rec = compositeProduct.getRecommendations().get(0);
        Event<Integer, Product> expectedRecommendationEvent = new Event(Event.Type.CREATE, compositeProduct.getProductId(), new Recommendation(compositeProduct.getProductId(), rec.getRecommendationId(), rec.getAuthor(), rec.getRate(), rec.getContent(), null));
        assertThat(queueRecommendations, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        assertEquals(1, queueReviews.size());
        ReviewSummary rev = compositeProduct.getReviews().get(0);
        Event<Integer, Product> expectedReviewEvent = new Event(Event.Type.CREATE, compositeProduct.getProductId(), new Review(compositeProduct.getProductId(), rev.getReviewId(), rev.getAuthor(), rev.getSubject(), rev.getContent(), null));
        assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    public void deleteCompositeProduct(){
        deleteAndVerifyProduct(1, HttpStatus.OK);

        assertEquals(1, queueProducts.size());
        Event<Integer, Product> expectedEvent = new Event(Event.Type.DELETE, 1, null);
        assertThat(queueProducts, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

        assertEquals(1, queueRecommendations.size());
        Event<Integer, Product> expectedRecommendationEvent = new Event(Event.Type.DELETE, 1, null);
        assertThat(queueRecommendations, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        assertEquals(1, queueReviews.size());
        Event<Integer, Product> expectedReviewEvent = new Event(Event.Type.DELETE, 1, null);
        assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        webTestClient.delete()
            .uri("/product-composite/" + productId)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
    }

    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
        webTestClient.post()
            .uri("/product-composite")
            .body(Mono.just(compositeProduct), ProductAggregate.class)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
    }

    private BlockingQueue<Message<?>> getQueue(MessageChannel messageChannel) {
        return collector.forChannel(messageChannel);
    }
}
