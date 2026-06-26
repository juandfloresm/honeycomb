Running:

    $ ./gradlew build && docker compose build && docker compose up -d

Shutting down:

    $ docker compose down

Step **ONE**: (in progress)

- Three core autonomous microservices.
- One composite microservice that aggregates data for the core microservices.
- The core microservices connect persist data to MongoDB and MySQL.
- Using non-blocking communication from composite microservice to core microservices.
- Synchronous communication (reading operation) uses Reactive (Flux, Mono) technology.
- Asynchronous communication uses RabbitMQ for publish/subscribe pattern with partitions.

Step **TWO**: (pending)

- Discovery server pattern
- Edge server pattern
- Security: Authentication & Authorization

Step **THREE**: (pending)

- Centralized configuration pattern
- Resilience mechanisms
- Tracing distributed landscape

Step **FOUR**: (pending)

- Orchestration
- Packaging

