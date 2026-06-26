package es.flores.microservices.core.explanation;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static es.flores.api.event.Event.Type.CREATE;
import static es.flores.api.event.Event.Type.DELETE;

import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import es.flores.microservices.core.explanation.persistence.*;
import es.flores.api.core.explanation.Explanation;
import es.flores.api.event.Event;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
  "spring.cloud.stream.defaultBinder=rabbit",
  "logging.level.es.flores=DEBUG"})
class ExplanationServiceApplicationTests extends MySqlTestBase {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ExplanationRepository repository;

  @Autowired
  @Qualifier("messageProcessor")
  private Consumer<Event<Integer, Explanation>> messageProcessor;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();
  }

  @Test
  void getExplanationsByScreenId() {
    int screenId = 1;
    
    assertEquals(0, repository.findByScreenId(screenId).size());

    sendCreateExplanationEvent(screenId, 1);
    sendCreateExplanationEvent(screenId, 2);
    sendCreateExplanationEvent(screenId, 3);

    assertEquals(3, repository.findByScreenId(screenId).size());

    getAndVerifyExplanationsByScreenId(screenId, OK)
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("$[2].screenId").isEqualTo(screenId)
      .jsonPath("$[2].explanationId").isEqualTo(3);
  }

  private WebTestClient.BodyContentSpec getAndVerifyExplanationsByScreenId(int screenId, HttpStatus expectedStatus) {
    return getAndVerifyExplanationsByScreenId("?screenId=" + screenId, expectedStatus);
  }

	private WebTestClient.BodyContentSpec getAndVerifyExplanationsByScreenId(String screenIdQuery, HttpStatus expectedStatus) {
    return client.get()
      .uri("/explanation" + screenIdQuery)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }

	private void sendCreateExplanationEvent(int screenId, int explanationId) {
    Explanation explanation = new Explanation(screenId, explanationId, "SA");
    Event<Integer, Explanation> event = new Event<>(CREATE, screenId, explanation);
    messageProcessor.accept(event);
  }

}
