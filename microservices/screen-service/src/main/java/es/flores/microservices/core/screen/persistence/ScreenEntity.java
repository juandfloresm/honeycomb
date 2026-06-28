package es.flores.microservices.core.screen.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "screens")
public class ScreenEntity {
    @Id private String id;

    @Version private Integer version;

    @Indexed(unique = true)
    private int screenId;
}