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
        recommendationRepository.deleteAll();

        RecommendationEntity recommendation = new RecommendationEntity(1, 1, "test", 90, "test content");
        savedEntity = recommendationRepository.save(recommendation);

        assertEqualsRecommendation(recommendation, savedEntity);
    }

    @Test
    @DisplayName("상품 리뷰 등록 테스트")
    public void create(){
        RecommendationEntity newEntity = new RecommendationEntity(1, 2, "test", 30, "test content");
        recommendationRepository.save(newEntity);

        RecommendationEntity foundEntity = recommendationRepository.findById(newEntity.getId()).get();
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, recommendationRepository.count());
    }

    @Test
    @DisplayName("상품 리뷰 수정 테스트")
    public void update(){
        savedEntity.setAuthor("test2");
        recommendationRepository.save(savedEntity);

        RecommendationEntity foundEntity = recommendationRepository.findById(savedEntity.getId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("test2", foundEntity.getAuthor());
    }

    @Test
    @DisplayName("상품 리뷰 삭제 테스트")
    public void delete(){
        recommendationRepository.delete(savedEntity);
        assertFalse(recommendationRepository.existsById(savedEntity.getId()));
    }

    @Test
    @DisplayName("상품 리뷰 조회")
    public void getByProductId(){
        List<RecommendationEntity> entityList = recommendationRepository.findByProductId(savedEntity.getProductId());

        assertEquals(entityList.size(), 1);
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test
    @DisplayName("유니크 값(productId, recommendationId) 검증")
    public void duplicateError(){
        RecommendationEntity entity = new RecommendationEntity(1, 1, "test", 30, "test content");
        assertThrows(DuplicateKeyException.class, () -> {
            recommendationRepository.save(entity);
        });
    }

    @Test
    public void optimisticLockError(){
        RecommendationEntity entity1 = recommendationRepository.findById(savedEntity.getId()).get();
        RecommendationEntity entity2 = recommendationRepository.findById(savedEntity.getId()).get();

        entity1.setAuthor("test1");
        recommendationRepository.save(entity1);

        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("test2");
            recommendationRepository.save(entity2);
        });

        RecommendationEntity updatedEntity = recommendationRepository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("test1", updatedEntity.getAuthor());
    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity){
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
