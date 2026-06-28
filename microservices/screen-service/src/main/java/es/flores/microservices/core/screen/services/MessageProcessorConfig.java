package es.flores.microservices.core.screen.services;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import es.flores.api.core.screen.Screen;
import es.flores.api.core.screen.ScreenService;
import es.flores.api.event.Event;
import es.flores.api.exceptions.EventProcessingException;

@Slf4j
@AllArgsConstructor
@Configuration
public class MessageProcessorConfig {
    
    private final ScreenService screenService;

    @Bean
    public Consumer<Event<Integer, Screen>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {
                case CREATE:
                    Screen screen = event.getData();
                    log.info("Create screen with ID: {}", screen.getScreenId());
                    screenService.createScreen(screen).block();
                    break;
                case DELETE:
                    int screenId = event.getKey();
                    log.info("Delete screen with ScreenID: {}", screenId);
                    screenService.deleteScreen(screenId).block();
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