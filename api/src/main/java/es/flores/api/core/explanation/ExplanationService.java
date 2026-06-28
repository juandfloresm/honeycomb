package es.flores.api.core.explanation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExplanationService {

	Mono<Explanation> createExplanation(Explanation body);

	/**
	 * Sample usage: "curl $HOST:$PORT/explanation?screenId=1".
	 *
	 * @param screenId Id of the screen
	 * @return the explanations of the screen
	 */
	@GetMapping(value = "/explanation", produces = "application/json")
	Flux<Explanation> getExplanations(@RequestParam(value = "screenId", required = true) int screenId);

	/**
	 * Sample usage: "curl -X DELETE $HOST:$PORT/explanation?screenId=1".
	 *
	 * @param screenId Id of the screen
	 */
	Mono<Void> deleteExplanations(int screenId);

}