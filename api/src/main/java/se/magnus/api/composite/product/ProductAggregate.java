package se.magnus.api.composite.product;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class ProductAggregate {
    private final int productId;
    private final String name;
    private final int weight;
    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;

    public ProductAggregate(){
        this.productId = 0;
        this.name = null;
        this.weight = 0;
        this.recommendations = null;
        this.reviews = null;
        this.serviceAddresses = null;
    }
}
