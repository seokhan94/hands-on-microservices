package se.magnus.microservices.core.product.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
@Slf4j
public class MessageProcessor {

    private final ProductService productService;

    public MessageProcessor(ProductService productService) {
        this.productService = productService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Product> event){
        log.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()){
            case CREATE:
                Product product = event.getData();
                log.info("Create product with ID: {}", product.getProductId());
                productService.createProduct(product);
                break;
            case DELETE:
                int productId = event.getKey();
                log.info("Delete recommendations with ProductID: {}", productId);
                productService.deleteProduct(productId);
                break;
            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                log.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        log.info("Message processing done!");
    }
}
