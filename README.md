# Microservices to the Cloud with Java & Spring Boot (Event-Driven)

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Microservices](https://img.shields.io/badge/Microservices-Event--Driven-orange)](https://microservices.io/)
[![Circuit Breaker](https://img.shields.io/badge/Circuit_Breaker-Resilience4J-red)](https://resilience4j.readme.io/docs/circuitbreaker)
[![Kafka](https://img.shields.io/badge/Kafka-Apache-red)](https://kafka.apache.org/)
[![Cloud](https://img.shields.io/badge/Cloud-AWS%20/Google%20Cloud%20/Other-lightgrey)](#)

## Project Background

This project was developed in 2023 to provide a radio station with the necessary microservices deployed on the AWS cloud. It uses an event-driven architecture combined with circuit breaker patterns to build resilient, scalable services.

Due to restrictions on U.S. dollar transactions in Argentina during this period, there was a need to explore and switch to equivalent open-source services to avoid dependency on cloud-native services that were either costly or difficult to access. This led to leveraging tools like Apache Kafka and Zookeeper for event messaging and coordination while maintaining cloud deployment readiness.

## Overview

This project demonstrates building robust microservices deployed to the cloud using **Java** and **Spring Boot** within an **event-driven architecture**. It implements essential patterns such as **circuit breaker** for fault tolerance, providing resilience in distributed systems.

The architecture leverages **Apache Kafka** for asynchronous event-driven communication between services, with **Zookeeper** for Kafka cluster coordination and management.

Using Spring Cloud, Resilience4J, and Kafka, this project showcases how to build microservices that communicate asynchronously via events, gracefully handle failures via circuit breakers, and scale on cloud environments.

## Key Features

- Java 17+ with Spring Boot 3.5.0 for rapid development and deployment
- Event-driven microservices architecture leveraging Apache Kafka as message broker
- Zookeeper used for Kafka cluster coordination
- Spring Cloud Circuit Breaker with Resilience4J for handling service failures and preventing cascading errors
- Robust handling of API communication with fallback methods to maintain system availability
- Deployment-ready for cloud platforms (AWS, Azure, GCP, or private clouds)
- Exemplifies microservices best practices including loose coupling, scalability, and fault tolerance

## Getting Started

### Prerequisites

- Java 17 or later
- Maven or Gradle build tools
- Docker (optional, for containerized deployment)
- Access to a cloud environment or local Kubernetes for deployment
- Apache Kafka and Zookeeper installed or containerized for event-driven communication

### Running Locally

1. Clone the repository:
git clone https://github.com/yourusername/your-microservices-project.git
cd your-microservices-project

2. Build the project:
./mvnw clean install

3. Run the services:
./mvnw spring-boot:run

### Circuit Breaker Example

Circuit breaker pattern is implemented using Resilience4J integrated via Spring Cloud Circuit Breaker to avoid cascading failures.

@Service
public class SampleService {
private final ReactiveCircuitBreaker circuitBreaker;
public SampleService(ReactiveCircuitBreakerFactory cbFactory) {
    this.circuitBreaker = cbFactory.create("sampleCircuitBreaker");
}

public Mono<String> callExternalService() {
    return circuitBreaker.run(
        webClient.get().uri("/external-api")
            .retrieve().bodyToMono(String.class),
        throwable -> Mono.just("Fallback response")
    );
}
}

### Event-Driven Communication

Microservices communicate asynchronously through events emitted and consumed via message brokers, ensuring decoupled and scalable services.

## Contributing

Contributions are welcome! Please open issues or submit pull requests to improve functionality, add features, or fix bugs.

If you find this project useful, please give it a ⭐ to show your support, and feel free to collaborate with contributions or suggestions if you're feeling fancy!

## License

This project is licensed under the MIT License.


