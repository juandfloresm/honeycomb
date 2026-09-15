**Start**:

    $ ./gradlew build && docker compose build && docker compose up -d

**Tests**

    $ ./gradlew clean test

**API Documentation**:

Since we are using Swagger UI interface to discover and test the API, along documentation for endpoints:

    http://localhost:8080/openapi/swagger-ui/index.html

**Stop**:

    $ docker compose down

**Roadmap**

* Step **ONE**: (in progress)
  - Three core autonomous microservices.
  - The core microservices connect persist data to MongoDB and MySQL.
  - Using non-blocking communication from composite microservice to core microservices.
  - Synchronous communication (reading operation) uses Reactive (Flux, Mono) technology.
  - Asynchronous communication uses RabbitMQ for publish/subscribe pattern with partitions.
    <br /><br />
* Step **TWO**: (pending)
  - Discovery server pattern
  - Edge server pattern
  - Security: Authentication & Authorization
    <br /><br />
* Step **THREE**: (pending)
  - Centralized configuration pattern
  - Resilience mechanisms
  - Tracing distributed landscape
    <br /><br />
* Step **FOUR**: (pending)
  - Orchestration
  - Packaging
    <br /><br />
