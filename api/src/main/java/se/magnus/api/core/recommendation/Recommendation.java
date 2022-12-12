package se.magnus.api.core.recommendation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Recommendation {
    private final int productId;
    private final int recommendationId;
    private final String author;
    private final int rate;
    private final String content;
    private final String serviceAddress;

    public Recommendation() {
        this.productId = 0;
        this.recommendationId = 0;
        this.author = null;
        this.rate = 0;
        this.content = null;
        this.serviceAddress = null;
    }
}
