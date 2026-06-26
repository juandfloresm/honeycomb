Building step **ONE**:

- Three core autonomous microservices.
- One composite microservice that aggregates data for the core microservices.
- The core microservices connect persist data to MongoDB and MySQL.
- Using non-blocking communication from composite microservice to core microservices.
- Synchronous communication (reading operation) uses Reactive (Flux, Mono) technology.
- Asynchronous communication uses RabbitMQ for publish/subscribe pattern with partitions.