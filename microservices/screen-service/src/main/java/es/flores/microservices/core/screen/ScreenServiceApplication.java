package es.flores.microservices.core.screen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("es.flores")
public class ScreenServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScreenServiceApplication.class, args);
	}

}
