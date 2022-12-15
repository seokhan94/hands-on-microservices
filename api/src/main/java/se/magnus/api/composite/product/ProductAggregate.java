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
    private final List<RecommendationSummary> recommendationSummaries;
    private final List<ReviewSummary> reviewSummaries;
    private final ServiceAddresses serviceAddresses;
}
