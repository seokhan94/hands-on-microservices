package se.magnus.microservices.core.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.services.RecommendationMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTests {
    private RecommendationMapper recommendationMapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    @DisplayName("api -> entity, entity -> api 변환 테스트")
    public void mapperTests(){

        assertNotNull(recommendationMapper);

        Recommendation api = new Recommendation(1, 2, "test", 4, "test content", "adr");
        RecommendationEntity entity = recommendationMapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRating());
        assertEquals(api.getContent(), entity.getContent());

        Recommendation api2 = recommendationMapper.entityToApi(entity);
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getRate(), api2.getRate());
        assertEquals(api.getContent(), api2.getContent());
        assertNull(api2.getServiceAddress());
    }

    @Test
    @DisplayName("apiList -> entityList, entityList -> apiList 변환 테스트")
    public void mapperListTests(){
        assertNotNull(recommendationMapper);

        Recommendation api = new Recommendation(1, 2, "test", 4, "test Content", "adr");
        Recommendation api2 = new Recommendation(1, 3, "test author", 4, "test Content", "adr");

        List<Recommendation> apiList = List.of(api, api2);

        List<RecommendationEntity> entityList = recommendationMapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        RecommendationEntity entity = entityList.get(0);
        RecommendationEntity entity2 = entityList.get(1);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRating());
        assertEquals(api.getContent(), entity.getContent());

        assertEquals(api2.getProductId(), entity2.getProductId());
        assertEquals(api2.getRecommendationId(), entity2.getRecommendationId());
        assertEquals(api2.getAuthor(), entity2.getAuthor());
        assertEquals(api2.getRate(), entity2.getRating());
        assertEquals(api2.getContent(), entity2.getContent());

        List<Recommendation> api2List = recommendationMapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());


        Recommendation api3 = api2List.get(0);
        Recommendation api4 = api2List.get(1);

        assertEquals(api.getProductId(), api3.getProductId());
        assertEquals(api.getRecommendationId(), api3.getRecommendationId());
        assertEquals(api.getAuthor(), api3.getAuthor());
        assertEquals(api.getRate(), api3.getRate());
        assertEquals(api.getContent(), api3.getContent());
        assertNull(api3.getServiceAddress());


        assertEquals(api2.getProductId(), api4.getProductId());
        assertEquals(api2.getRecommendationId(), api4.getRecommendationId());
        assertEquals(api2.getAuthor(), api4.getAuthor());
        assertEquals(api2.getRate(), api4.getRate());
        assertEquals(api2.getContent(), api4.getContent());
        assertNull(api4.getServiceAddress());
    }
}
