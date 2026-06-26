package es.flores.api.composite.screen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ScreenAggregate {
  private final int screenId;
  private final List<ComponentSummary> components;
  private final List<ExplanationSummary> explanations;
  private final ServiceAddresses serviceAddresses;
}