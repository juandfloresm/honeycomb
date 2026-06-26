package es.flores.api.core.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Component {
  private int screenId;
  private int componentId;
  private String serviceAddress;
}