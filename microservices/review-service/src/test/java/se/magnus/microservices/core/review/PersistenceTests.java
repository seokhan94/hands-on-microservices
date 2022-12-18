package se.magnus.microservices.core.review;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.persistence.ReviewRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ExtendWith(SpringExtension.class)
public class PersistenceTests {

    @Autowired
    private ReviewRepository reviewRepository;

    private ReviewEntity savedEntity;

    @BeforeEach
    public void setupDB(){
        reviewRepository.deleteAll();

        ReviewEntity entity = new ReviewEntity(1, 1, "test author", "test subject", "test content");
        savedEntity = reviewRepository.save(entity);

        assertEqualsReview(entity, savedEntity);
    }

    @Test
    @DisplayName("상품 리뷰 추가 단위 테스트")
    public void create(){
        ReviewEntity newEntity = new ReviewEntity(1, 2, "test author", "test subject", "test content");
        reviewRepository.save(newEntity);

        ReviewEntity foundEntity = reviewRepository.findById(newEntity.getId()).get();
        assertEqualsReview(newEntity, foundEntity);

        assertEquals(2, reviewRepository.count());
    }

    @Test
    @DisplayName("상품 리뷰 수정 단위 테스트")
    public void update(){
        savedEntity.setAuthor("test2 author");
        reviewRepository.save(savedEntity);

        ReviewEntity foundEntity = reviewRepository.findById(savedEntity.getId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("test2 author", foundEntity.getAuthor());
    }

    @Test
    @DisplayName("상품 리뷰 삭제 단위 테스트")
    public void delete(){
        reviewRepository.delete(savedEntity);
        assertFalse(reviewRepository.existsById(savedEntity.getId()));
    }

    @Test
    @DisplayName("상품 리뷰 조회 단위 테스트")
    public void getByProductId(){
        List<ReviewEntity> entityList = reviewRepository.findByProductId(savedEntity.getProductId());

        assertEquals(1, entityList.size());
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test
    @DisplayName("상품 리뷰 중복키 단위 테스트")
    public void duplicateError(){
        ReviewEntity entity = new ReviewEntity(1, 1, "1", "2", "3");
        assertThrows(DataIntegrityViolationException.class, () -> {
            reviewRepository.save(entity);
        });
    }

    @Test
    @DisplayName("낙관적 락 매커니즘 단위 테스트")
    public void optimisticLockError(){

        ReviewEntity entity1 = reviewRepository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = reviewRepository.findById(savedEntity.getId()).get();

        entity1.setAuthor("test1 author");
        reviewRepository.save(entity1);

        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("test2 author");
            reviewRepository.save(entity2);
        });

        ReviewEntity updatedEntity = reviewRepository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("test1 author", updatedEntity.getAuthor());
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(),        actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),   actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getReviewId(),  actualEntity.getReviewId());
        assertEquals(expectedEntity.getAuthor(),    actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(),   actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(),   actualEntity.getContent());
    }
}
