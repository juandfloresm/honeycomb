package es.flores.microservices.core.explanation.services;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import es.flores.api.core.explanation.Explanation;
import es.flores.microservices.core.explanation.persistence.ExplanationEntity;

@Mapper(componentModel = "spring")
public interface ExplanationMapper {

  @Mappings({
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Explanation entityToApi(ExplanationEntity entity);

  @Mappings({
    @Mapping(target = "id", ignore = true),
    @Mapping(target = "version", ignore = true)
  })
  ExplanationEntity apiToEntity(Explanation api);

  List<Explanation> entityListToApiList(List<ExplanationEntity> entity);

  List<ExplanationEntity> apiListToEntityList(List<Explanation> api);
}