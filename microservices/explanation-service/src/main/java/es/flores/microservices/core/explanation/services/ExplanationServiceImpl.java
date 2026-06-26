package es.flores.microservices.core.explanation.services;

import static java.util.logging.Level.FINE;

import java.util.List;
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

@RestController
public class ExplanationServiceImpl implements ExplanationService {

  private static final Logger LOG = LoggerFactory.getLogger(ExplanationServiceImpl.class);

  private final ExplanationRepository repository;

  private final ExplanationMapper mapper;

  private final ServiceUtil serviceUtil;

  private final Scheduler jdbcScheduler;

  public ExplanationServiceImpl(@Qualifier("jdbcScheduler") Scheduler jdbcScheduler, ExplanationRepository repository, ExplanationMapper mapper, ServiceUtil serviceUtil) {
    this.jdbcScheduler = jdbcScheduler;
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

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
      LOG.debug("createExplanation: created a explanation entity: {}/{}", body.getScreenId(), body.getExplanationId());
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
    LOG.info("Will get explanations for screen with id={}", screenId);
    return Mono.fromCallable(() -> internalGetExplanations(screenId))
      .flatMapMany(Flux::fromIterable)
      .log(LOG.getName(), FINE)
      .subscribeOn(jdbcScheduler);
  }

  private List<Explanation> internalGetExplanations(int screenId) {
    List<ExplanationEntity> entityList = repository.findByScreenId(screenId);
    List<Explanation> list = mapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
    LOG.debug("Response size: {}", list.size());
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
    LOG.debug("deleteExplanations: tries to delete explanations for the screen with screenId: {}", screenId);
    repository.deleteAll(repository.findByScreenId(screenId));
  }
}