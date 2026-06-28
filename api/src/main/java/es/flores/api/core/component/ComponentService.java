package es.flores.api.core.component;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ComponentService {

	Mono<Component> createComponent(Component body);

	/**
	 * Sample usage: "curl $HOST:$PORT/component?screenId=1".
	 *
	 * @param screenId Id of the screen
	 * @return the components of the screen
	 */
	@GetMapping(value = "/component", produces = "application/json")
	Flux<Component> getComponents(@RequestParam(value = "screenId", required = true) int screenId);

	/**
	 * Sample usage: "curl -X DELETE $HOST:$PORT/component?screenId=1".
	 *
	 * @param screenId Id of the screen
	 */
	Mono<Void> deleteComponents(int screenId);

}