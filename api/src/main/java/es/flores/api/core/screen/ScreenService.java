package es.flores.api.core.screen;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface ScreenService {

  Mono<Screen> createScreen(Screen body);

  /**
   * Sample usage: "curl $HOST:$PORT/screen/1".
   *
   * @param screenId Id of the screen
   * @return the screen, if found, else null
   */
  @GetMapping(
    value = "/screen/{screenId}",
    produces = "application/json")
  Mono<Screen> getScreen(@PathVariable int screenId);

  /**
   * Sample usage: "curl -X DELETE $HOST:$PORT/screen?screenId=1".
   *
   * @param screenId Id of the screen
   */
  Mono<Void> deleteScreen(int screenId);

}