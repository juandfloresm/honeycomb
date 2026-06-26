package es.flores.api.composite.screen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "ScreenComposite", description = "REST API for composite screen information.")
public interface ScreenCompositeService {

  /**
   * Sample usage, see below.
   *
   * curl -X POST $HOST:$PORT/screen-composite \
   *   -H "Content-Type: application/json" --data \
   *   '{"screenId":123,"name":"screen 123","weight":123}'
   *
   * @param body A JSON representation of the new composite screen
   */
  @Operation(
    summary = "${api.screen-composite.create-composite-screen.description}",
    description = "${api.screen-composite.create-composite-screen.notes}")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
    @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PostMapping(
    value    = "/screen-composite",
    consumes = "application/json")
  Mono<Void> createScreen(@RequestBody ScreenAggregate body);

  /**
   * Sample usage: "curl $HOST:$PORT/screen-composite/1".
   *
   * @param screenId Id of the screen
   * @return the composite screen info, if found, else null
   */
  @Operation(
    summary = "${api.screen-composite.get-composite-screen.description}",
    description = "${api.screen-composite.get-composite-screen.notes}")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
    @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
    @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
    @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @GetMapping(
    value = "/screen-composite/{screenId}",
    produces = "application/json")
  Mono<ScreenAggregate> getScreen(@PathVariable int screenId);

  /**
   * Sample usage: "curl -X DELETE $HOST:$PORT/screen-composite/1".
   *
   * @param screenId Id of the screen
   */
  @Operation(
    summary = "${api.screen-composite.delete-composite-screen.description}",
    description = "${api.screen-composite.delete-composite-screen.notes}")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
    @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @ResponseStatus(HttpStatus.ACCEPTED)
  @DeleteMapping(value = "/screen-composite/{screenId}")
  Mono<Void> deleteScreen(@PathVariable int screenId);
}