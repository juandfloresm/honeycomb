package es.flores.microservices.core.screen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

@SpringBootApplication
@ComponentScan("es.flores")
public class ScreenServiceApplication {

	@Autowired
    ReactiveMongoOperations mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(ScreenServiceApplication.class, args);
	}

}
