package es.flores.microservices.core.screen.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import es.flores.api.core.screen.Screen;
import es.flores.microservices.core.screen.persistence.ScreenEntity;

@Mapper(componentModel = "spring")
public interface ScreenMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Screen entityToApi(ScreenEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true)
    })
    ScreenEntity apiToEntity(Screen api);

}