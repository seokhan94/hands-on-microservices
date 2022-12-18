package se.magnus.microservices.core.review.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.persistence.ReviewRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ReviewServiceImpl implements ReviewService {

    private final ServiceUtil serviceUtil;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        List<Review> reviews = reviewRepository.findByProductId(productId)
                .stream()
                .map(reviewMapper::entityToApi)
                .collect(Collectors.toList());

        reviews.forEach(r -> r.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getReviews response size: {}", reviews.size());

        return reviews;
    }

    @Override
    public Review createReview(Review body) {
        try {
            ReviewEntity entity = reviewMapper.apiToEntity(body);
            ReviewEntity savedEntity = reviewRepository.save(entity);

            log.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return reviewMapper.entityToApi(savedEntity);

        }catch (DataIntegrityViolationException e){
            throw new InvalidInputException("Duplicate key, Product Id:" + body.getProductId() + ", Review id: " + body.getReviewId());
        }

    }

    @Override
    public void deleteReviews(int productId) {
        log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        reviewRepository.deleteAll(reviewRepository.findByProductId(productId));
    }
}
