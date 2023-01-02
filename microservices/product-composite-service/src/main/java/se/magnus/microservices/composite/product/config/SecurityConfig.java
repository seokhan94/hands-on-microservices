package se.magnus.microservices.composite.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http){
        http
            .authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/product-composite/**").hasAnyAuthority("SCOPE_product:write")
                .pathMatchers(HttpMethod.DELETE, "/product-composite/**").hasAnyAuthority("SCOPE_product:write")
                .pathMatchers(HttpMethod.GET, "/product-composite/**").hasAnyAuthority("SCOPE_product:read")
                .anyExchange().authenticated()
                .and()
            .oauth2ResourceServer()
                .jwt();

        return http.build();
    }
}
