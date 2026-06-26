package es.flores.microservices.core.screen.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ScreenRepository extends ReactiveCrudRepository<ScreenEntity, String> {
    Mono<ScreenEntity> findByScreenId(int screenId);
}