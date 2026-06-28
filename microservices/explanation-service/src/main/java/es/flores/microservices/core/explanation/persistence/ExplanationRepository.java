package es.flores.microservices.core.explanation.persistence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ExplanationRepository extends CrudRepository<ExplanationEntity, Integer> {
  @Transactional(readOnly = true)
  List<ExplanationEntity> findByScreenId(int screenId);
}