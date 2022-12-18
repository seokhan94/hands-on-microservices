package se.magnus.microservices.core.recommendation.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private final ServiceUtil serviceUtil;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationMapper recommendationMapper;
    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        List<RecommendationEntity> entityList = recommendationRepository.findByProductId(productId);
        List<Recommendation> recommendationList = recommendationMapper.entityListToApiList(entityList);
        recommendationList.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getRecommendations response size: {}", recommendationList.size());
        return recommendationList;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            RecommendationEntity entity = recommendationMapper.apiToEntity(body);
            RecommendationEntity savedEntity = recommendationRepository.save(entity);

            log.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return recommendationMapper.entityToApi(savedEntity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id: " + body.getRecommendationId());
        }

    }

    @Override
    public void deleteRecommendations(int productId) {
        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);

        recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId));
    }
}
