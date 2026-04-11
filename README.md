# Radio Station Microservices

Plataforma de radio distribuida construida con **Java 17**, **Spring Boot 3.2**, **Spring WebFlux** y **Apache Kafka en modo KRaft** (sin ZooKeeper). La infraestructura corre dentro de un contenedor **LXD/LXC** con Docker, manteniendo el host limpio.

---

## Documentación

| Doc | Descripción |
|---|---|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Diagrama del sistema, flujo de eventos, stack por capa, tabla de puertos |
| [INFRASTRUCTURE.md](docs/INFRASTRUCTURE.md) | Paso a paso: crear contenedor LXC, instalar Docker, levantar Kafka KRaft, ejecutar servicios, configurar IntelliJ |

---

## Estructura del proyecto

```
radio-station-microservices/
├── pom.xml                          ← Parent POM (gestión centralizada de versiones)
├── docker/
│   └── docker-compose.yml           ← Kafka KRaft cluster
├── docs/
│   ├── ARCHITECTURE.md              ← Diagrama y flujo del sistema
│   └── INFRASTRUCTURE.md            ← Setup paso a paso
└── services/
    ├── program-service/             ← Productor de eventos (puerto 8081)
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/radiostation/program/
    │       │   ├── ProgramServiceApplication.java
    │       │   ├── api/
    │       │   │   └── ProgramController.java   ← REST endpoints
    │       │   ├── domain/
    │       │   │   └── Program.java             ← Entidad de dominio
    │       │   └── messaging/                   ← (pendiente: KafkaSender)
    │       └── resources/application.yml
    └── subscription-service/        ← Consumidor reactivo de eventos (puerto 8082)
        ├── pom.xml
        └── src/main/
            ├── java/com/radiostation/subscription/
            │   ├── SubscriptionServiceApplication.java
            │   └── messaging/
            │       ├── KafkaReceiverConfig.java       ← Configuración del receiver reactivo
            │       └── SubscriptionEventConsumer.java ← Consumidor non-blocking
            └── resources/application.yml
```

---

## API (`program-service`)

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/programs?name={nombre}` | Crea un programa y publica evento en Kafka |
| `GET` | `/programs/ping` | Health check — responde `pong` |

---

## Quick Start

```bash
git clone https://github.com/tuni56/radio-station-microservices.git
cd radio-station-microservices
```

Ver [INFRASTRUCTURE.md](docs/INFRASTRUCTURE.md) para el setup completo del entorno.

---

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2 / Spring WebFlux |
| Mensajería | Apache Kafka KRaft (sin ZooKeeper) |
| Reactive Kafka | reactor-kafka 1.3.23 |
| Resiliencia | Resilience4J + Spring Cloud 2023.0.0 |
| Infraestructura | LXD/LXC + Docker |
| Build | Maven 3.9 |

---

**Rocío Baigorria** | Data Engineer & Platform Architect
