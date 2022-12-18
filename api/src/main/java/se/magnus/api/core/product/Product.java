package se.magnus.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Product {
    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;

    public Product() {
        this.productId = 0;
        this.name = null;
        this.weight = 0;
        this.serviceAddress = null;
    }

}
