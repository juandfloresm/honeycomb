package es.flores.microservices.core.explanation.persistence;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "UI_explanations")
@Data
public class ExplanationEntity {
  @Id @GeneratedValue
  private int id;

  @Version
  private int version;

  private int screenId;
  private int explanationId;
}