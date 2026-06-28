package es.flores.microservices.core.explanation.services;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import es.flores.api.core.explanation.Explanation;
import es.flores.api.core.explanation.ExplanationService;
import es.flores.api.event.Event;
import es.flores.api.exceptions.EventProcessingException;

@Slf4j
@AllArgsConstructor
@Configuration
public class MessageProcessorConfig {

  private final ExplanationService explanationService;

  @Bean
  public Consumer<Event<Integer, Explanation>> messageProcessor() {
    return event -> {
      log.info("Process message created at {}...", event.getEventCreatedAt());

      switch (event.getEventType()) {
        case CREATE:
          Explanation explanation = event.getData();
          log.info("Create explanation with ID: {}/{}", explanation.getScreenId(), explanation.getExplanationId());
          explanationService.createExplanation(explanation).block();
          break;

        case DELETE:
          int screenId = event.getKey();
          log.info("Delete explanations with ScreenID: {}", screenId);
          explanationService.deleteExplanations(screenId).block();
          break;

        default:
          String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
          log.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      log.info("Message processing done!");
    };
  }
}