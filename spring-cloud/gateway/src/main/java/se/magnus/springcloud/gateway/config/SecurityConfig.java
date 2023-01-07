package se.magnus.springcloud.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http){
        http
            .csrf().disable()
            .authorizeExchange()
                .pathMatchers("/headerrouting/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/config/**").permitAll()
                .pathMatchers("/eureka/**").permitAll()
                .pathMatchers("/oauth/**").permitAll()
                .anyExchange().authenticated()
            .and()
                .oauth2ResourceServer()
                .jwt();

        return http.build();
    }
}
