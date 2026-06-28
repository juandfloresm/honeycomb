package es.flores.microservices.core.explanation.services;

import static java.util.logging.Level.FINE;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import es.flores.api.core.explanation.Explanation;
import es.flores.api.core.explanation.ExplanationService;
import es.flores.api.exceptions.InvalidInputException;
import es.flores.microservices.core.explanation.persistence.ExplanationEntity;
import es.flores.microservices.core.explanation.persistence.ExplanationRepository;
import es.flores.util.http.ServiceUtil;

@Slf4j
@AllArgsConstructor
@RestController
public class ExplanationServiceImpl implements ExplanationService {

  private final Scheduler jdbcScheduler;
  private final ExplanationRepository repository;
  private final ExplanationMapper mapper;
  private final ServiceUtil serviceUtil;

  @Override
  public Mono<Explanation> createExplanation(Explanation body) {
    if (body.getScreenId() < 1) {
      throw new InvalidInputException("Invalid screenId: " + body.getScreenId());
    }
    return Mono.fromCallable(() -> internalCreateExplanation(body))
            .subscribeOn(jdbcScheduler);
  }

  private Explanation internalCreateExplanation(Explanation body) {
    try {
      ExplanationEntity entity = mapper.apiToEntity(body);
      ExplanationEntity newEntity = repository.save(entity);
      log.debug("createExplanation: created a explanation entity: {}/{}", body.getScreenId(), body.getExplanationId());
      return mapper.entityToApi(newEntity);
    } catch (DataIntegrityViolationException dive) {
      throw new InvalidInputException("Duplicate key, Screen Id: " + body.getScreenId() + ", Explanation Id:" + body.getExplanationId());
    }
  }

  @Override
  public Flux<Explanation> getExplanations(int screenId) {
    if (screenId < 1) {
      throw new InvalidInputException("Invalid screenId: " + screenId);
    }
    log.info("Will get explanations for screen with id={}", screenId);
    return Mono.fromCallable(() -> internalGetExplanations(screenId))
      .flatMapMany(Flux::fromIterable)
      .log(log.getName(), FINE)
      .subscribeOn(jdbcScheduler);
  }

  private List<Explanation> internalGetExplanations(int screenId) {
    List<ExplanationEntity> entityList = repository.findByScreenId(screenId);
    List<Explanation> list = mapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
    log.debug("Response size: {}", list.size());
    return list;
  }

  @Override
  public Mono<Void> deleteExplanations(int screenId) {
    if (screenId < 1) {
      throw new InvalidInputException("Invalid screenId: " + screenId);
    }
    return Mono.fromRunnable(() -> internalDeleteExplanations(screenId)).subscribeOn(jdbcScheduler).then();
  }

  private void internalDeleteExplanations(int screenId) {
    log.debug("deleteExplanations: tries to delete explanations for the screen with screenId: {}", screenId);
    repository.deleteAll(repository.findByScreenId(screenId));
  }
}