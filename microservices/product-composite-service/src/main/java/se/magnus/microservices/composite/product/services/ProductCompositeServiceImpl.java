package se.magnus.microservices.composite.product.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.composite.product.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration productCompositeIntegration;

    @Override
    public ProductAggregate getCompositeProduct(int productId) {
        log.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

        Product product = productCompositeIntegration.getProduct(productId);
        if(product == null) throw new NotFoundException("No product found for productId: " + productId);

        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productId);
        List<Review> reviews = productCompositeIntegration.getReviews(productId);

        log.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);
        return createProductAggregation(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void createCompositeProduct(ProductAggregate body) {
        log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

        Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
        productCompositeIntegration.createProduct(product);

        if(body.getRecommendations() != null){
            body.getRecommendations().forEach(r -> {
                Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                productCompositeIntegration.createRecommendation(recommendation);
            });
        }
        if(body.getReviews() != null){
            body.getReviews().forEach(r -> {
                Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                productCompositeIntegration.createReview(review);
            });
        }

        log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId() );
    }

    @Override
    public void deleteCompositeProduct(int productId) {
        log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        productCompositeIntegration.deleteProduct(productId);

        productCompositeIntegration.deleteRecommendations(productId);

        productCompositeIntegration.deleteReviews(productId);

        log.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregation(Product product, List<Recommendation> recommendations, List<Review> reviews, String compositeAddress) {
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
            recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                .collect(Collectors.toList());

        ServiceAddresses serviceAddresses = ServiceAddresses.builder()
            .compositeAddress(compositeAddress)
            .productAddress(product.getServiceAddress())
            .recommendationAddress(
                recommendations != null && recommendations.size() > 0
                    ? recommendations.stream().findFirst().get().getServiceAddress()
                    : ""
            )
            .reviewAddress(
                reviews != null && reviews.size() > 0
                    ? reviews.stream().findFirst().get().getServiceAddress()
                    : ""
            )
            .build();

        return ProductAggregate.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .weight(product.getWeight())
                .recommendations(recommendationSummaries)
                .reviews(reviewSummaries)
                .serviceAddresses(serviceAddresses)
                .build();
    }
}
