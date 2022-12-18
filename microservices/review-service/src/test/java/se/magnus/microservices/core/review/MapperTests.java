package se.magnus.microservices.core.review;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.review.Review;
import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.services.ReviewMapper;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTests {

    private ReviewMapper reviewMapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    @DisplayName("api -> entity, entity -> api 변환 테스트")
    public void test1(){

        assertNotNull(reviewMapper);

        Review api = new Review(1, 1, "test author", "test subject", "test content", "test adr");
        ReviewEntity entity = reviewMapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getSubject(), entity.getSubject());
        assertEquals(api.getContent(), entity.getContent());
        
        Review api2 = reviewMapper.entityToApi(entity);
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getReviewId(), api2.getReviewId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getSubject(), api2.getSubject());
        assertEquals(api.getContent(), api2.getContent());
        assertNull(api2.getServiceAddress());
    }
}
