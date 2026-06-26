package es.flores.api.core.explanation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Explanation {
  private int screenId;
  private int explanationId;
  private String serviceAddress;
}