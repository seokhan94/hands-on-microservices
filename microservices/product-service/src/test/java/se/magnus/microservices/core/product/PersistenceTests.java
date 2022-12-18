package se.magnus.microservices.core.product;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@DataMongoTest
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
public class PersistenceTests {

    @Autowired
    private ProductRepository productRepository;
    private ProductEntity savedEntity;

    @BeforeEach
    public void setupDb(){
        productRepository.deleteAll();

        ProductEntity product = new ProductEntity(1, "n", 1);
        savedEntity = productRepository.save(product);
        assertEqualsProduct(product, savedEntity);
    }

    @Test
    @DisplayName("상품 생성")
    public void create(){
        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        savedEntity = productRepository.save(newEntity);

        ProductEntity foundEntity = productRepository.findByProductId(newEntity.getProductId()).get();
        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, productRepository.count());
    }

    @Test
    @DisplayName("상품 수정")
    public void update(){
        savedEntity.setName("n2");
        productRepository.save(savedEntity);

        ProductEntity foundEntity = productRepository.findByProductId(savedEntity.getProductId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    @DisplayName("상품 삭제")
    public void delete(){
        productRepository.delete(savedEntity);
        assertFalse(productRepository.existsById(savedEntity.getId()));
    }

    @Test
    @DisplayName("상품 찾기")
    public void getByProductId(){
        Optional<ProductEntity> entity = productRepository.findByProductId(savedEntity.getProductId());
        assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    @Test
    public void duplicateError(){
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
        assertThrows(DuplicateKeyException.class, () -> {
            productRepository.save(entity);
        });
    }

    @Test
    @DisplayName("낙관적 잠근 매커니즘")
    public void optimisticLockError(){

        // 데이터베이스에서 가져온 엔티티를 변수 2개에 저장
        ProductEntity entity1 = productRepository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = productRepository.findById(savedEntity.getId()).get();

        // 첫 번째 엔티티 객체를 업데이트
        entity1.setName("n1");
        productRepository.save(entity1);

        // 두 번째 엔티티 객체를 업데이트
        // 두 번째 엔티티 객체의 버전이 낮으므로 실패
        // 즉 낙관적 잠금 오류 발생해 실패
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setName("n2");
            productRepository.save(entity2);
        });

        ProductEntity updatedEntity = productRepository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

    @Test
    @DisplayName("정렬 및 페이징 기능 테스트")
    public void paging(){
        productRepository.deleteAll();
        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
                .mapToObj(
                        i -> new ProductEntity(i, "name " + i, i)
                )
                .collect(Collectors.toList());
        productRepository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");

        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productEntityPage = productRepository.findAll(nextPage);
        assertEquals(expectedProductIds, productEntityPage.getContent().stream().map(ProductEntity::getProductId).collect(Collectors.toList()).toString());
        assertEquals(expectsNextPage, productEntityPage.hasNext());
        return productEntityPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity product, ProductEntity savedEntity) {
        assertEquals(product.getId(), savedEntity.getId());
        assertEquals(product.getVersion(), savedEntity.getVersion());
        assertEquals(product.getProductId(), savedEntity.getProductId());
        assertEquals(product.getName(), savedEntity.getName());
        assertEquals(product.getWeight(), savedEntity.getWeight());
    }
}
