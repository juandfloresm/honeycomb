package es.flores.microservices.core.explanation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import reactor.core.scheduler.Schedulers;
import reactor.core.scheduler.Scheduler;

@Slf4j
@SpringBootApplication
@ComponentScan("es.flores")
public class ExplanationServiceApplication {
  private final Integer threadPoolSize;
  private final Integer taskQueueSize;

  public ExplanationServiceApplication(
    @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
    @Value("${app.taskQueueSize:100}") Integer taskQueueSize
  ) {
    this.threadPoolSize = threadPoolSize;
    this.taskQueueSize = taskQueueSize;
  }

  @Bean
  public Scheduler jdbcScheduler() {
    log.info("Creates a jdbcScheduler with thread pool size = {}", threadPoolSize);
    return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "jdbc-pool");
  }

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(ExplanationServiceApplication.class, args);
    String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
    log.info("Connected to MySQL: " + mysqlUri);
  }
}
