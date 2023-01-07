package se.magnus.microservices.core.review.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Component
@Slf4j
public class JdbcSchedulerConfig {

    @Value("${spring.datasource.maximum-pool-size:10}")
    private Integer connectionPoolSize;

    @Bean
    public Scheduler jdbcScheduler(){
        log.info("Creates a jdbcScheduler with connectionPoolSize = " + connectionPoolSize);
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }
}
