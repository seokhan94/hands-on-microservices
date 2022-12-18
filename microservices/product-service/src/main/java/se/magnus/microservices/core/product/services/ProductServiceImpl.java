package se.magnus.microservices.core.product.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Product getProduct(int productId) {
        log.debug("/product return the found product for productId={}", productId);

        if(productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        ProductEntity entity = productRepository.findByProductId(productId).orElseThrow(() ->
                new NotFoundException("No product found for productId: " + productId));

        Product response = productMapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());
        return response;
    }

    @Override
    public Product createProduct(Product body) {
        try {
            ProductEntity entity = productMapper.apiToEntity(body);
            ProductEntity newEntity = productRepository.save(entity);
            return productMapper.entityToApi(newEntity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
        }
    }

    @Override
    public void deleteProduct(int productId) {
        productRepository.findByProductId(productId).ifPresent(e -> productRepository.delete(e));
    }
}
