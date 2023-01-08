package se.magnus.api.core.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

    /**
     *
     * @param productId
     * @return Mono<Product>
     */
    @GetMapping(value = "/product/{productId}", produces = "application/json")
    Mono<Product> getProduct(
            @PathVariable int productId,
            @RequestParam(value = "delay", required = false, defaultValue = "0") int delay, // 응답 지연 Param
            @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent // 예외 발생 비율 Param
    );

    @PostMapping(
            value = "/product",
            consumes = "application/json",
            produces = "application/json")
    Product createProduct(@RequestBody Product body);

    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable int productId);
}
