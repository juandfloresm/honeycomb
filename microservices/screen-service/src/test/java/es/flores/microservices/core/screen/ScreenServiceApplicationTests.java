package es.flores.microservices.core.screen;

import es.flores.api.core.screen.Screen;
import es.flores.api.event.Event;
import es.flores.microservices.core.screen.persistence.ScreenRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static es.flores.api.event.Event.Type.CREATE;
import static es.flores.api.event.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScreenServiceApplicationTests extends MongoDbTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ScreenRepository repository;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, Screen>> messageProcessor;

	@BeforeEach
	void setupDb() {
		repository.deleteAll().block();
	}

	@Test
	void getScreenById() {
		int screenId = 1;
		assertNull(repository.findByScreenId(screenId).block());
		assertEquals(0, (long) repository.count().block());
		log.info(repository.count().block().toString());
	}

	@Test
	void getScreenInvalidParameterNegativeValue() {
		int screenIdInvalid = -1;
		getAndVerifyScreen(screenIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/screen/" + screenIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid screenId: " + screenIdInvalid);
	}

	@Test
	void deleteScreen() {
		int screenId = 1;
		sendCreateScreenEvent(screenId);
		assertNotNull(repository.findByScreenId(screenId).block());
		sendDeleteScreenEvent(screenId);
		assertNull(repository.findByScreenId(screenId).block());
		sendDeleteScreenEvent(screenId);
	}

	private WebTestClient.BodyContentSpec getAndVerifyScreen(int screenId, HttpStatus expectedStatus) {
		return getAndVerifyScreen("/" + screenId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyScreen(String screenIdPath, HttpStatus expectedStatus) {
		return client.get()
				.uri("/screen" + screenIdPath)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateScreenEvent(int screenId) {
		Screen screen = new Screen(screenId, "SA");
		Event<Integer, Screen> event = new Event<>(CREATE, screenId, screen);
		messageProcessor.accept(event);
	}

	private void sendDeleteScreenEvent(int screenId) {
		Event<Integer, Screen> event = new Event<>(DELETE, screenId, null);
		messageProcessor.accept(event);
	}
}
