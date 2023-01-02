package se.magnus.microservices.composite.product.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.product.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.http.ServiceUtil;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final SecurityContext nullSC = new SecurityContextImpl();
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration productCompositeIntegration;

    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {

        return Mono
                .zip(
                    values -> createProductAggregation((SecurityContext) values[0], (Product) values[1], (List<Recommendation>) values[2], (List<Review>) values[3], serviceUtil.getServiceAddress()),
                    ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
                    productCompositeIntegration.getProduct(productId),
                    productCompositeIntegration.getRecommendations(productId).collectList(),
                    productCompositeIntegration.getReviews(productId).collectList()
                )
                .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                .log();
    }

    @Override
    public Mono<Void> createCompositeProduct(ProductAggregate body) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnSuccess(securityContext -> internalCreateCompositeProduct(securityContext, body))
                .then();

    }

    public void internalCreateCompositeProduct(SecurityContext securityContext, ProductAggregate body){
        try {

            logAuthorizationInfo(securityContext);

            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            productCompositeIntegration.createProduct(product);

            if(body.getRecommendations() != null){
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    productCompositeIntegration.createRecommendation(recommendation);
                });
            }
            if(body.getReviews() != null){
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    productCompositeIntegration.createReview(review);
                });
            }

            log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId() );
        }catch (RuntimeException runtimeException){
            log.warn("createCompositeProduct failed: {}", runtimeException.toString());
            throw runtimeException;
        }
    }

    @Override
    public Mono<Void> deleteCompositeProduct(int productId) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnSuccess(securityContext -> internalDeleteCompositeProduct(securityContext, productId))
                .then();
    }

    public void internalDeleteCompositeProduct(SecurityContext securityContext, int productId){
        try {
            logAuthorizationInfo(securityContext);

            log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

            productCompositeIntegration.deleteProduct(productId);

            productCompositeIntegration.deleteRecommendations(productId);

            productCompositeIntegration.deleteReviews(productId);

            log.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
        }catch (RuntimeException runtimeException){
            log.warn("deleteCompositeProduct failed: {}", runtimeException.toString());
            throw runtimeException;
        }
    }

    private ProductAggregate createProductAggregation(SecurityContext securityContext, Product product, List<Recommendation> recommendations, List<Review> reviews, String compositeAddress) {

        logAuthorizationInfo(securityContext);

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
            recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                .collect(Collectors.toList());

        ServiceAddresses serviceAddresses = ServiceAddresses.builder()
            .compositeAddress(compositeAddress)
            .productAddress(product.getServiceAddress())
            .recommendationAddress(
                recommendations != null && recommendations.size() > 0
                    ? recommendations.stream().findFirst().get().getServiceAddress()
                    : ""
            )
            .reviewAddress(
                reviews != null && reviews.size() > 0
                    ? reviews.stream().findFirst().get().getServiceAddress()
                    : ""
            )
            .build();

        return ProductAggregate.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .weight(product.getWeight())
                .recommendations(recommendationSummaries)
                .reviews(reviewSummaries)
                .serviceAddresses(serviceAddresses)
                .build();
    }

    private void logAuthorizationInfo(SecurityContext securityContext){
        if(securityContext != null && securityContext.getAuthentication() != null && securityContext.getAuthentication() instanceof JwtAuthenticationToken){
            Jwt jwtToken = ((JwtAuthenticationToken) securityContext.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        }else{
            log.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt){
        if(jwt != null){
            log.warn("No JWT supplied, running tests are we?");
        }else {
            if(log.isDebugEnabled()){
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scopes");
                Object expires = jwt.getClaims().get("exp");

                log.debug("Authorization info: Subject: {}, scopes: {}, expires: {}, issuer: {}, audiences: {}", subject, scopes, expires, issuer, audience);
            }
        }
    }
}
