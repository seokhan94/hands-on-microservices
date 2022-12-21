//package se.magnus.microservices.core.product;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Mono;
//import se.magnus.api.core.product.Product;
//import se.magnus.microservices.core.product.persistence.ProductRepository;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.springframework.http.HttpStatus.*;
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
//class ProductServiceApplicationTests {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//
//    @BeforeEach
//    public void setUpDb(){
//        productRepository.deleteAll();
//    }
//
//    @Test
//    public void getProductById(){
//        int productId = 1;
//
//        postAndVerifyProduct(productId, OK);
//
//        assertTrue(productRepository.findByProductId(productId).isPresent());
//
//        getAndVerifyProduct(productId, OK)
//                .jsonPath("$.productId").isEqualTo(productId);
////        webTestClient.get()
////            .uri("/product/" + productId)
////            .accept(MediaType.APPLICATION_JSON)
////            .exchange()
////            .expectStatus().isOk()
////            .expectHeader().contentType(MediaType.APPLICATION_JSON)
////            .expectBody()
////            .jsonPath("$.productId").isEqualTo(productId);
//    }
//
//    @Test
//    public void duplicateError(){
//        int productId = 1;
//
//        postAndVerifyProduct(productId, OK);
//
//        assertTrue(productRepository.findByProductId(productId).isPresent());
//
//        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
//                .jsonPath("$.path").isEqualTo("/product")
//                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id:" + productId);
//    }
//
//    @Test
//    public void deleteProduct(){
//        int productId = 1;
//
//        postAndVerifyProduct(productId, OK);
//        assertTrue(productRepository.findByProductId(productId).isPresent());
//
//        deleteAndVerifyProduct(productId,OK);
//        assertFalse(productRepository.findByProductId(productId).isPresent());
//
//        deleteAndVerifyProduct(productId, OK);
//    }
//
//
//    @Test
//    public void getProductInvalidParameterString(){
//
//        getAndVerifyProduct("/no-integer", BAD_REQUEST)
//                .jsonPath("$.path").isEqualTo("/product/no-integer");
////                .jsonPath("$.message").isEqualTo("Type mismatch.");
//
////        WebTestClient.BodyContentSpec equalTo = webTestClient.get()
////                .uri("/product/no-integer")
////                .accept(APPLICATION_JSON)
////                .exchange()
////                .expectStatus().isEqualTo(BAD_REQUEST)
////                .expectHeader().contentType(APPLICATION_JSON)
////                .expectBody()
////                .jsonPath("$.path").isEqualTo("/product/no-integer");
////                .jsonPath("$.message").isEqualTo("Type mismatch.");
//    }
//
//    @Test
//    public void getProductNotFound(){
//        int productIdNotFound = 13;
//
//
//        getAndVerifyProduct(productIdNotFound, NOT_FOUND)
//            .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
//            .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
//
////        webTestClient.get()
////            .uri("/product/" + productIdNotFound)
////            .accept(MediaType.APPLICATION_JSON)
////            .exchange()
////            .expectStatus().isNotFound()
////            .expectHeader().contentType(MediaType.APPLICATION_JSON)
////            .expectBody()
////            .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
////            .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
//    }
//
//    @Test
//    public void getProductInvalidParameterNegativeValue(){
//        int productIdInvalid = -1;
//
//        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
//            .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
//            .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
//
////        webTestClient.get()
////            .uri("/product/" + productIdInvalid)
////            .accept(MediaType.APPLICATION_JSON)
////            .exchange()
////            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
////            .expectHeader().contentType(MediaType.APPLICATION_JSON)
////            .expectBody()
////            .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
////            .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
//    }
//
//    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus){
//        return getAndVerifyProduct("/" + productId, expectedStatus);
//    }
//
//    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus){
//        return webTestClient.get()
//                .uri("/product" + productIdPath)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(expectedStatus)
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody();
//    }
//
//    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus){
//        Product product = new Product(productId, "Name " + productId, productId, "SA");
//        return webTestClient.post()
//                .uri("/product")
//                .body(Mono.just(product), Product.class)
//                .accept(APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(expectedStatus)
//                .expectHeader().contentType(APPLICATION_JSON)
//                .expectBody();
//    }
//
//    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus){
//        return webTestClient.delete()
//                .uri("/product/" + productId)
//                .accept(APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(expectedStatus)
//                .expectBody();
//    }
//}
