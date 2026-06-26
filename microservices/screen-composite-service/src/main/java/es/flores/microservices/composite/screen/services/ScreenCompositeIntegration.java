package es.flores.microservices.composite.screen.services;

import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;
import static es.flores.api.event.Event.Type.CREATE;
import static es.flores.api.event.Event.Type.DELETE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import es.flores.api.core.screen.Screen;
import es.flores.api.core.screen.ScreenService;
import es.flores.api.core.component.ComponentService;
import es.flores.api.core.component.Component;
import es.flores.api.core.explanation.Explanation;
import es.flores.api.core.explanation.ExplanationService;
import es.flores.api.event.Event;
import es.flores.api.exceptions.InvalidInputException;
import es.flores.api.exceptions.NotFoundException;
import es.flores.util.http.HttpErrorInfo;

@Service
public class ScreenCompositeIntegration implements ScreenService, ComponentService, ExplanationService {

  private static final Logger LOG = LoggerFactory.getLogger(ScreenCompositeIntegration.class);

  private final WebClient webClient;
  private final ObjectMapper mapper;

  private final String screenServiceUrl;
  private final String componentServiceUrl;
  private final String explanationServiceUrl;

  private final StreamBridge streamBridge;

  private final Scheduler publishEventScheduler;

  public ScreenCompositeIntegration(
    @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,

    WebClient.Builder webClient,
    ObjectMapper mapper,
    StreamBridge streamBridge,

    @Value("${app.screen-service.host}") String screenServiceHost,
    @Value("${app.screen-service.port}") int  screenServicePort,

    @Value("${app.component-service.host}") String componentServiceHost,
    @Value("${app.component-service.port}") int  componentServicePort,

    @Value("${app.explanation-service.host}") String explanationServiceHost,
    @Value("${app.explanation-service.port}") int  explanationServicePort
  ) {

    this.publishEventScheduler = publishEventScheduler;
    this.webClient = webClient.build();
    this.mapper = mapper;
    this.streamBridge = streamBridge;

    screenServiceUrl = "http://" + screenServiceHost + ":" + screenServicePort;
    componentServiceUrl = "http://" + componentServiceHost + ":" + componentServicePort;
    explanationServiceUrl = "http://" + explanationServiceHost + ":" + explanationServicePort;
  }

  @Override
  public Mono<Screen> createScreen(Screen body) {
    return Mono.fromCallable(() -> {
      sendMessage("screens-out-0", new Event<>(CREATE, body.getScreenId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Mono<Screen> getScreen(int screenId) {
    String url = screenServiceUrl + "/screen/" + screenId;
    LOG.debug("Will call the getScreen API on URL: {}", url);

    return webClient.get().uri(url).retrieve().bodyToMono(Screen.class).log(LOG.getName(), FINE).onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public Mono<Void> deleteScreen(int screenId) {
    return Mono.fromRunnable(() -> sendMessage("screens-out-0", new Event<>(DELETE, screenId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  @Override
  public Mono<Component> createComponent(Component body) {
    return Mono.fromCallable(() -> {
      sendMessage("components-out-0", new Event<>(CREATE, body.getScreenId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Flux<Component> getComponents(int screenId) {
    String url = componentServiceUrl + "/component?screenId=" + screenId;
    LOG.debug("Will call the getComponents API on URL: {}", url);
    // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
    return webClient.get().uri(url).retrieve().bodyToFlux(Component.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
  }

  @Override
  public Mono<Void> deleteComponents(int screenId) {

    return Mono.fromRunnable(() -> sendMessage("components-out-0", new Event<>(DELETE, screenId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  @Override
  public Mono<Explanation> createExplanation(Explanation body) {
    return Mono.fromCallable(() -> {
      sendMessage("explanations-out-0", new Event<>(CREATE, body.getScreenId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Flux<Explanation> getExplanations(int screenId) {
    String url = explanationServiceUrl + "/explanation?screenId=" + screenId;
    LOG.debug("Will call the getExplanations API on URL: {}", url);
    // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
    return webClient.get().uri(url).retrieve().bodyToFlux(Explanation.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
  }

  @Override
  public Mono<Void> deleteExplanations(int screenId) {
    return Mono.fromRunnable(() -> sendMessage("explanations-out-0", new Event<>(DELETE, screenId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  public Mono<Health> getScreenHealth() {
    return getHealth(screenServiceUrl);
  }

  public Mono<Health> getExplanationHealth() {
    return getHealth(explanationServiceUrl);
  }

  private Mono<Health> getHealth(String url) {
    url += "/actuator/health";
    LOG.debug("Will call the Health API on URL: {}", url);
    return webClient.get().uri(url).retrieve().bodyToMono(String.class)
      .map(s -> new Health.Builder().up().build())
      .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
      .log(LOG.getName(), FINE);
  }

  private void sendMessage(String bindingName, Event event) {
    LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
    Message message = MessageBuilder.withPayload(event)
      .setHeader("partitionKey", event.getKey())
      .build();
    streamBridge.send(bindingName, message);
  }

  private Throwable handleException(Throwable ex) {
    if (!(ex instanceof WebClientResponseException)) {
      LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
      return ex;
    }

    WebClientResponseException wcre = (WebClientResponseException)ex;

    switch (HttpStatus.resolve(wcre.getStatusCode().value())) {

      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(wcre));

      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(wcre));

      default:
        LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
        LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
        return ex;
    }
  }

  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }
}