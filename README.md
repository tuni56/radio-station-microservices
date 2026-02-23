# Radio Station Platform: Resilience through Event-Driven Architecture

### 🎙️ Project Vision
Built as a response to real-world economic constraints, this project demonstrates a high-availability microservices architecture designed to run on **AWS** while remaining cloud-agnostic through open-source tooling.

By leveraging **Apache Kafka** and **ZooKeeper**, I replaced costly cloud-native messaging services with a self-managed, high-throughput event backbone, ensuring operational continuity during a period of restricted cloud spend in Argentina.

---

### 🛠️ Key Architectural Patterns

**Event-Driven Communication:**  
Decoupled services communicate asynchronously using **Kafka**, enabling radio station metadata and streaming events to be processed with millisecond latency while maintaining loose coupling between services.

**Resilience & Fault Tolerance:**  
Integrated **Resilience4J** implementing the **Circuit Breaker pattern** to prevent cascading failures. When a dependency becomes unavailable, the system preserves availability through controlled degradation and fallback responses.

**Service Coordination:**  
Used **ZooKeeper** for distributed coordination and synchronization, simulating the reliability typically provided by managed cloud infrastructure within a self-hosted environment.

**Reactive Mindset:**  
Implemented `ReactiveCircuitBreaker` to support non-blocking I/O operations, improving scalability and optimizing resource utilization under load.

---

### 🚀 Technical Stack

- **Language:** Java 17+
- **Framework:** Spring Boot 3.x / Spring Cloud
- **Messaging:** Apache Kafka & ZooKeeper
- **Resilience:** Resilience4J
- **Build Tool:** Maven

---

### 🧩 Circuit Breaker Implementation (Code Highlight)

The platform ensures system stability by wrapping external API calls inside a circuit breaker. This prevents a single slow or failing dependency from impacting the entire radio network.

```java
public Mono<String> callExternalService() {
    return circuitBreaker.run(
        webClient.get()
            .uri("/external-api")
            .retrieve()
            .bodyToMono(String.class),
        throwable -> Mono.just("Service temporarily unavailable (Fallback Mode)")
    );
}
```

---

### 💡 Why this matters for Data Engineering

This project provided a deep understanding of **Data Origin**. By architecting the backend systems that generate events, downstream Data Lakes and Streaming Pipelines (Python/AWS) can be built with stronger guarantees because the event producers, schemas, and failure modes of source systems are fully understood.

This perspective enables:

- Better schema design  
- Reliable streaming ingestion  
- Improved observability of data pipelines  
- Stronger resilience across the data lifecycle  

---

### 📦 Quick Start

1. **Clone & Build**
```bash
git clone https://github.com/tuni56/radio-station-microservices.git
cd radio-station-microservices
./mvnw clean install
```

2. **Infrastructure:** Ensure Kafka and ZooKeeper are running on default ports.

3. **Run:** Execute the following inside each service module:

```bash
./mvnw spring-boot:run
```

---

**Rocío Baigorria** | Data Engineer & Platform Architect
