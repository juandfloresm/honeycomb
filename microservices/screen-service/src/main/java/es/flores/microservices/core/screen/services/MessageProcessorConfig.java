package es.flores.microservices.core.screen.services;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import es.flores.api.core.screen.Screen;
import es.flores.api.core.screen.ScreenService;
import es.flores.api.event.Event;
import es.flores.api.exceptions.EventProcessingException;

@Configuration
public class MessageProcessorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final ScreenService screenService;

    public MessageProcessorConfig(ScreenService screenService) {
        this.screenService = screenService;
    }

    @Bean
    public Consumer<Event<Integer, Screen>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {
                case CREATE:
                    Screen screen = event.getData();
                    LOG.info("Create screen with ID: {}", screen.getScreenId());
                    screenService.createScreen(screen).block();
                    break;
                case DELETE:
                    int screenId = event.getKey();
                    LOG.info("Delete screen with ScreenID: {}", screenId);
                    screenService.deleteScreen(screenId).block();
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