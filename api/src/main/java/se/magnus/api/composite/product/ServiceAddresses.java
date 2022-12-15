package se.magnus.api.composite.product;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class ServiceAddresses {
    private final String compositeAddress;
    private final String productAddress;
    private final String reviewAddress;
    private final String recommendationAddress;

    public ServiceAddresses() {
        this.compositeAddress = null;
        this.productAddress = null;
        this.reviewAddress = null;
        this.recommendationAddress = null;
    }
}
