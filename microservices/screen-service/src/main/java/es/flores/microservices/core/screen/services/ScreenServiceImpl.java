package es.flores.microservices.core.screen.services;

import static java.util.logging.Level.FINE;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import es.flores.api.core.screen.Screen;
import es.flores.api.core.screen.ScreenService;
import es.flores.api.exceptions.InvalidInputException;
import es.flores.api.exceptions.NotFoundException;
import es.flores.microservices.core.screen.persistence.ScreenEntity;
import es.flores.microservices.core.screen.persistence.ScreenRepository;
import es.flores.util.http.ServiceUtil;

@Slf4j
@AllArgsConstructor
@RestController
public class ScreenServiceImpl implements ScreenService {

    private final ServiceUtil serviceUtil;
    private final ScreenRepository repository;
    private final ScreenMapper mapper;

    @Override
    public Mono<Screen> createScreen(Screen body) {
        if (body.getScreenId() < 1) {
            throw new InvalidInputException("Invalid screenId: " + body.getScreenId());
        }

        ScreenEntity entity = mapper.apiToEntity(body);
        Mono<Screen> newEntity = repository.save(entity)
                .log(log.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Screen Id: " + body.getScreenId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity;
    }

    @Override
    public Mono<Screen> getScreen(int screenId) {
        if (screenId < 1) {
            throw new InvalidInputException("Invalid screenId: " + screenId);
        }

        log.info("Will get screen info for id={}", screenId);

        return repository.findByScreenId(screenId)
                .switchIfEmpty(Mono.error(new NotFoundException("No screen found for screenId: " + screenId)))
                .log(log.getName(), FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> setServiceAddress(e));
    }

    @Override
    public Mono<Void> deleteScreen(int screenId) {
        if (screenId < 1) {
            throw new InvalidInputException("Invalid screenId: " + screenId);
        }
        log.debug("deleteScreen: tries to delete an entity with screenId: {}", screenId);
        return repository.findByScreenId(screenId).log(log.getName(), FINE).map(e -> repository.delete(e)).flatMap(e -> e);
    }

    private Screen setServiceAddress(Screen e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}