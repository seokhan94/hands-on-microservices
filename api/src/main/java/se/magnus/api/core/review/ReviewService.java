package se.magnus.api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface ReviewService {

    @GetMapping(value = "/review", produces = "application/json")
    Flux<Review> getReviews(@RequestParam int productId);

    @PostMapping(
            value = "/review",
            consumes = "application/json",
            produces = "application/json"
    )
    Review createReview(@RequestBody Review body);

    @DeleteMapping(value = "/review", produces = "application/json")
    void deleteReviews(@RequestParam int productId);
}
