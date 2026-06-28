package es.flores.microservices.composite.screen.services;

import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import es.flores.api.composite.screen.*;
import es.flores.api.core.screen.Screen;
import es.flores.api.core.component.Component;
import es.flores.api.core.explanation.Explanation;
import es.flores.util.http.ServiceUtil;

@Slf4j
@AllArgsConstructor
@RestController
public class ScreenCompositeServiceImpl implements ScreenCompositeService {

  private final ServiceUtil serviceUtil;
  private final ScreenCompositeIntegration integration;

  @Override
  public Mono<Void> createScreen(ScreenAggregate body) {
    try {
      List<Mono> monoList = new ArrayList<>();

      log.info("Will create a new composite entity for screen.id: {}", body.getScreenId());

      Screen screen = new Screen(body.getScreenId(), null);
      monoList.add(integration.createScreen(screen));

      if (body.getComponents() != null) {
        body.getComponents().forEach(r -> {
          Component component = new Component(body.getScreenId(), r.getComponentId(), null);
          monoList.add(integration.createComponent(component));
        });
      }

      if (body.getExplanations() != null) {
        body.getExplanations().forEach(r -> {
          Explanation explanation = new Explanation(body.getScreenId(), r.getExplanationId(), null);
          monoList.add(integration.createExplanation(explanation));
        });
      }

      log.debug("createCompositeScreen: composite entities created for screenId: {}", body.getScreenId());

      return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
        .doOnError(ex -> log.warn("createCompositeScreen failed: {}", ex.toString()))
        .then();

    } catch (RuntimeException re) {
      log.warn("createCompositeScreen failed: {}", re.toString());
      throw re;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Mono<ScreenAggregate> getScreen(int screenId) {
    log.info("Will get composite screen info for screen.id={}", screenId);
    return Mono.zip(
      values -> createScreenAggregate((Screen) values[0], (List<Component>) values[1], (List<Explanation>) values[2], serviceUtil.getServiceAddress()),
      integration.getScreen(screenId),
      integration.getComponents(screenId).collectList(),
      integration.getExplanations(screenId).collectList())
      .doOnError(ex -> log.warn("getCompositeScreen failed: {}", ex.toString()))
      .log(log.getName(), FINE);
  }

  @Override
  public Mono<Void> deleteScreen(int screenId) {
    try {
      log.info("Will delete a screen aggregate for screen.id: {}", screenId);
      return Mono.zip(
        r -> "",
        integration.deleteScreen(screenId),
        integration.deleteComponents(screenId),
        integration.deleteExplanations(screenId))
        .doOnError(ex -> log.warn("delete failed: {}", ex.toString()))
        .log(log.getName(), FINE).then();
    } catch (RuntimeException re) {
      log.warn("deleteCompositeScreen failed: {}", re.toString());
      throw re;
    }
  }

  private ScreenAggregate createScreenAggregate(Screen screen, List<Component> components, List<Explanation> explanations, String serviceAddress) {
    // 1. Setup screen info
    int screenId = screen.getScreenId();

    // 2. Copy summary component info, if available
    List<ComponentSummary> componentSummaries = (components == null) ? null :
       components.stream()
        .map(r -> new ComponentSummary(r.getComponentId()))
        .collect(Collectors.toList());

    // 3. Copy summary explanation info, if available
    List<ExplanationSummary> explanationSummaries = (explanations == null)  ? null :
      explanations.stream()
        .map(r -> new ExplanationSummary(r.getExplanationId()))
        .collect(Collectors.toList());

    // 4. Create info regarding the involved microservices addresses
    String screenAddress = screen.getServiceAddress();
    String explanationAddress = (explanations != null && explanations.size() > 0) ? explanations.get(0).getServiceAddress() : "";
    String componentAddress = (components != null && components.size() > 0) ? components.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, screenAddress, explanationAddress, componentAddress);

    return new ScreenAggregate(screenId, componentSummaries, explanationSummaries, serviceAddresses);
  }
}