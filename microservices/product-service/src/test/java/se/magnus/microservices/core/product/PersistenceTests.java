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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;

@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
public class PersistenceTests {

    @Autowired
    private ProductRepository productRepository;
    private ProductEntity savedEntity;

    @BeforeEach
    public void setupDb(){
        StepVerifier.create(productRepository.deleteAll()).verifyComplete();

        ProductEntity product = new ProductEntity(1, "n", 1);

        StepVerifier.create(productRepository.save(product))
                .expectNextMatches(createEntity -> {
                    savedEntity = createEntity;
                    return areProductEqual(product, savedEntity);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("상품 생성")
    public void create(){
        ProductEntity newEntity = new ProductEntity(2, "n", 2);

        StepVerifier.create(productRepository.save(newEntity))
            .expectNextMatches(createEntity -> newEntity.getProductId() == createEntity.getProductId())
            .verifyComplete();

        StepVerifier.create(productRepository.findById(newEntity.getId()))
            .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
            .verifyComplete();

        StepVerifier.create(productRepository.count())
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    @DisplayName("상품 수정")
    public void update(){
        savedEntity.setName("n2");

        StepVerifier.create(productRepository.save(savedEntity))
            .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
            .verifyComplete();

        StepVerifier.create(productRepository.findById(savedEntity.getId()))
            .expectNextMatches(foundEntity ->
                foundEntity.getVersion() == 1 &&
                foundEntity.getName().equals("n2"))
            .verifyComplete();
    }

    @Test
    @DisplayName("상품 삭제")
    public void delete(){
        StepVerifier.create(productRepository.delete(savedEntity))
            .verifyComplete();

        StepVerifier.create(productRepository.existsById(savedEntity.getId()))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    @DisplayName("상품 찾기")
    public void getByProductId(){
        StepVerifier.create(productRepository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    @DisplayName("상품 중복키 단위 테스트")
    public void duplicateError(){
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
        StepVerifier.create(productRepository.save(entity))
            .expectError(DuplicateKeyException.class)
            .verify();
    }

    @Test
    @DisplayName("낙관적 잠근 매커니즘")
    public void optimisticLockError(){

        // 데이터베이스에서 가져온 엔티티를 변수 2개에 저장
        ProductEntity entity1 = productRepository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = productRepository.findById(savedEntity.getId()).block();

        // 첫 번째 엔티티 객체를 업데이트
        entity1.setName("n1");
        productRepository.save(entity1).block();

        // 두 번째 엔티티 객체를 업데이트
        // 두 번째 엔티티 객체의 버전이 낮으므로 실패
        // 즉 낙관적 잠금 오류 발생해 실패

        entity2.setName("n2");
        StepVerifier.create(productRepository.save(entity2))
            .expectError(OptimisticLockingFailureException.class)
            .verify();


        StepVerifier.create(productRepository.findById(savedEntity.getId()))
            .expectNextMatches(foundEntity ->
                foundEntity.getVersion() == 1 &&
                foundEntity.getName().equals("n1"))
            .verifyComplete();
    }

//    @Test
//    @DisplayName("정렬 및 페이징 기능 테스트")
//    public void paging(){
//        productRepository.deleteAll();
//        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
//                .mapToObj(
//                        i -> new ProductEntity(i, "name " + i, i)
//                )
//                .collect(Collectors.toList());
//        productRepository.saveAll(newProducts);
//
//        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
//
//        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
//        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
//        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
//    }
//
//    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
//        Page<ProductEntity> productEntityPage = productRepository.findAll(nextPage);
//        assertEquals(expectedProductIds, productEntityPage.getContent().stream().map(ProductEntity::getProductId).collect(Collectors.toList()).toString());
//        assertEquals(expectsNextPage, productEntityPage.hasNext());
//        return productEntityPage.nextPageable();
//    }

    private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
        return(
            expectedEntity.getId().equals(actualEntity.getId()) &&
            expectedEntity.getVersion() == actualEntity.getVersion() &&
            expectedEntity.getProductId() == actualEntity.getProductId() &&
            expectedEntity.getName().equals(actualEntity.getName()) &&
            expectedEntity.getWeight() == actualEntity.getWeight()
        );
    }
}
