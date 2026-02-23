# Radio Station Platform: Resilience through Event-Driven Architecture

### 🎙️ Project Vision
Built as a response to real-world economic constraints, this project demonstrates a high-availability microservices architecture designed to run on **AWS** while remaining cloud-agnostic through open-source tooling. 

By leveraging **Apache Kafka and ZooKeeper**, I replaced costly cloud-native messaging services with a self-managed, high-throughput event backbone, ensuring 100% operational continuity during a period of restricted cloud spend in Argentina.
---

### 🛠️ Key Architectural Patterns

* **Event-Driven Communication:** Decoupled services using **Kafka** to handle asynchronous data flows, ensuring that the radio station's metadata and stream events are processed with millisecond latency.
* **Resilience & Fault Tolerance:** Integrated **Resilience4J** (Circuit Breaker pattern) to prevent cascading failures. If one service fails, the system maintains availability through intelligent fallbacks.
* **Service Coordination:** Used **ZooKeeper** to master distributed system synchronization, simulating the reliability of AWS-managed services in a custom environment.
* **Reactive Mindset:** Implementation of `ReactiveCircuitBreaker` for non-blocking I/O operations, optimizing resource utilization.

### 🚀 Technical Stack
* **Language:** Java 17+
* **Framework:** Spring Boot 3.x / Spring Cloud
* **Messaging:** Apache Kafka & ZooKeeper
* **Resilience:** Resilience4J
* **Build Tool:** Maven

---

### 🧩 Circuit Breaker Implementation (Code Highlight)
The platform ensures system stability by wrapping external API calls in a circuit breaker. This prevents a single slow dependency from bringing down the entire radio network.

```java

public Mono<String> callExternalService() {
    return circuitBreaker.run(
        webClient.get().uri("/external-api").retrieve().bodyToMono(String.class),
        throwable -> Mono.just("Service temporarily unavailable (Fallback Mode)")
    );
}

---

### 💡 Why this matters for Data Engineering
This project provided me with a deep understanding of **Data Origin**. By architecting the backend that generates events, I can design downstream Data Lakes and Streaming Pipelines (in Python/AWS) that are much more robust, as I intimately understand the producers, schemas, and failure modes of the source systems.

### 📦 Quick Start
1. **Clone & Build:**
   ```bash
   git clone [https://github.com/tuni56/radio-station-microservices.git](https://github.com/tuni56/radio-station-microservices.git)
   cd radio-station-microservices
   ./mvnw clean install


2. **Infrastructure:** Ensure Kafka and ZooKeeper are running on default ports.
3. **Run:** Execute `./mvnw spring-boot:run` within each service module.

---
**Rocío Baigorria** | Data Engineer & Platform Architect
