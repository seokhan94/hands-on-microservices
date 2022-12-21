package se.magnus.microservices.core.recommendation;

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
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@DataMongoTest
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
public class PersistenceTests {

    @Autowired
    private RecommendationRepository recommendationRepository;
    private RecommendationEntity savedEntity;


    @BeforeEach
    public void setUpDB(){
        StepVerifier.create(recommendationRepository.deleteAll())
                .verifyComplete();

        RecommendationEntity recommendation = new RecommendationEntity(1, 1, "test", 90, "test content");

        StepVerifier.create(recommendationRepository.save(recommendation))
                .expectNextMatches(createEntity -> {
                    savedEntity = createEntity;
                    return areRecommendationEqual(recommendation, savedEntity);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("상품 리뷰 등록 테스트")
    public void create(){
        RecommendationEntity newEntity = new RecommendationEntity(1, 2, "test", 30, "test content");

        StepVerifier.create(recommendationRepository.save(newEntity))
                .expectNextMatches(createEntity -> areRecommendationEqual(newEntity, createEntity))
                .verifyComplete();

        StepVerifier.create(recommendationRepository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> areRecommendationEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(recommendationRepository.count())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    @DisplayName("상품 리뷰 수정 테스트")
    public void update(){
        savedEntity.setAuthor("test2");

        StepVerifier.create(recommendationRepository.save(savedEntity))
            .expectNextMatches(updatedEntity -> updatedEntity.getAuthor().equals("test2"))
            .verifyComplete();

        StepVerifier.create(recommendationRepository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getAuthor().equals("test2"))
                .verifyComplete();
    }

    @Test
    @DisplayName("상품 리뷰 삭제 테스트")
    public void delete(){

        StepVerifier.create(recommendationRepository.delete(savedEntity))
                .verifyComplete();

        StepVerifier.create(recommendationRepository.existsById(savedEntity.getId()))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    @DisplayName("상품 리뷰 조회")
    public void getByProductId(){
        StepVerifier.create(recommendationRepository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(foundEntity -> areRecommendationEqual(savedEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    @DisplayName("유니크 값(productId, recommendationId) 검증")
    public void duplicateError(){
        RecommendationEntity entity = new RecommendationEntity(1, 1, "test", 30, "test content");
        StepVerifier.create(recommendationRepository.save(entity))
            .expectError(DuplicateKeyException.class)
            .verify();
    }

    @Test
    @DisplayName("낙관적 락 매커니즘 단위 테스트")
    public void optimisticLockError(){
        RecommendationEntity entity1 = recommendationRepository.findById(savedEntity.getId()).block();
        RecommendationEntity entity2 = recommendationRepository.findById(savedEntity.getId()).block();

        entity1.setAuthor("test1");
        recommendationRepository.save(entity1).block();


        entity2.setAuthor("test2");
        StepVerifier.create(recommendationRepository.save(entity2))
            .expectError(OptimisticLockingFailureException.class)
            .verify();

        StepVerifier.create(recommendationRepository.findById(savedEntity.getId()))
            .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getAuthor().equals("test1"))
            .verifyComplete();
    }

    private boolean areRecommendationEqual(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        return (
                expectedEntity.getId().equals(actualEntity.getId()) &&
                        expectedEntity.getVersion() == actualEntity.getVersion() &&
                        expectedEntity.getProductId() == actualEntity.getProductId() &&
                        expectedEntity.getRecommendationId() == actualEntity.getRecommendationId() &&
                        expectedEntity.getAuthor().equals(actualEntity.getAuthor()) &&
                        expectedEntity.getRating() == actualEntity.getRating() &&
                        expectedEntity.getContent().equals(actualEntity.getContent())
        );
    }
}
