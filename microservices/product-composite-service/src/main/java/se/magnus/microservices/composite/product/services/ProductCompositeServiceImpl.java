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
    public ProductAggregate getProduct(int productId) {
        Product product = productCompositeIntegration.getProduct(productId);
        if(product == null) throw new NotFoundException("No product found for productId: " + productId);

        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productId);
        List<Review> reviews = productCompositeIntegration.getReviews(productId);

        return createProductAggregation(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregation(Product product, List<Recommendation> recommendations, List<Review> reviews, String compositeAddress) {
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
            recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
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
                .recommendationSummaries(recommendationSummaries)
                .reviewSummaries(reviewSummaries)
                .serviceAddresses(serviceAddresses)
                .build();
    }
}
