package es.flores.microservices.core.explanation.services;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import es.flores.api.core.explanation.Explanation;
import es.flores.api.core.explanation.ExplanationService;
import es.flores.api.event.Event;
import es.flores.api.exceptions.EventProcessingException;

@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final ExplanationService explanationService;

  public MessageProcessorConfig(ExplanationService explanationService) {
    this.explanationService = explanationService;
  }

  @Bean
  public Consumer<Event<Integer, Explanation>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      switch (event.getEventType()) {

        case CREATE:
          Explanation explanation = event.getData();
          LOG.info("Create explanation with ID: {}/{}", explanation.getScreenId(), explanation.getExplanationId());
          explanationService.createExplanation(explanation).block();
          break;

        case DELETE:
          int screenId = event.getKey();
          LOG.info("Delete explanations with ScreenID: {}", screenId);
          explanationService.deleteExplanations(screenId).block();
          break;

        default:
          String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
          LOG.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("Message processing done!");
    };
  }
}