package es.flores.api.composite.screen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ServiceAddresses {
  private final String composite;
  private final String screen;
  private final String component;
  private final String explanation;
}