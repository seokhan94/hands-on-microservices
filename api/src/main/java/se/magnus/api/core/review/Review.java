package se.magnus.api.core.review;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Review {
    private final int productId;
    private final int reviewId;
    private final String author;
    private final String subject;
    private final String content;
    private final String serviceAddress;

    public Review() {
        this.productId = 0;
        this.reviewId = 0;
        this.author = null;
        this.subject = null;
        this.content = null;
        this.serviceAddress = null;
    }
}
