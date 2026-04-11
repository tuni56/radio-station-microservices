# Architecture Overview

## System Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  HOST — Ubuntu 24.04                                                        │
│                                                                             │
│  ┌─────────────┐    POST /programs?name=X    ┌──────────────────────────┐  │
│  │             │ ──────────────────────────► │    program-service       │  │
│  │  IntelliJ / │                             │    :8081                 │  │
│  │  curl       │ ◄────────────────────────── │                          │  │
│  │             │    {"id":"...","name":"X"}   │  ProgramController       │  │
│  └─────────────┘                             │    └─► Program (domain)  │  │
│                                              │    └─► KafkaSender (*)   │  │
│                                              └────────────┬─────────────┘  │
│                                                           │                │
│                                              KAFKA_BOOTSTRAP_SERVERS       │
│                                              <CONTAINER_IP>:29092          │
│                                                           │                │
└───────────────────────────────────────────────────────────┼────────────────┘
                                                            │
                              ┌─────────────────────────────┼────────────────────────────┐
                              │  LXC CONTAINER — radio-dev  │                            │
                              │                             ▼                            │
                              │              ┌──────────────────────────┐               │
                              │              │   Docker                 │               │
                              │              │  ┌────────────────────┐  │               │
                              │              │  │  kafka-kraft       │  │               │
                              │              │  │  confluentinc/     │  │               │
                              │              │  │  cp-kafka:7.6.0    │  │               │
                              │              │  │                    │  │               │
                              │              │  │  KRaft mode        │  │               │
                              │              │  │  (no ZooKeeper)    │  │               │
                              │              │  │                    │  │               │
                              │              │  │  INTERNAL :9092    │  │               │
                              │              │  │  EXTERNAL :29092 ──┼──┼── proxy ──►  │ :29092 (host)
                              │              │  │  CONTROLLER :9093  │  │               │
                              │              │  │                    │  │               │
                              │              │  │  topic:            │  │               │
                              │              │  │  program-events    │  │               │
                              │              │  │  (3 partitions)    │  │               │
                              │              │  └────────┬───────────┘  │               │
                              │              └───────────┼──────────────┘               │
                              │                          │                              │
                              │              INTERNAL listener :9092                    │
                              │              (si los servicios corren                   │
                              │               dentro del contenedor)                    │
                              │                          │                              │
                              └──────────────────────────┼──────────────────────────────┘
                                                         │
                                                         ▼ (consume)
                              ┌──────────────────────────────────────────────────────────┐
                              │  HOST (o contenedor, según opción elegida)               │
                              │                                                          │
                              │  ┌───────────────────────────────────────────────────┐  │
                              │  │  subscription-service  :8082                      │  │
                              │  │                                                   │  │
                              │  │  KafkaReceiverConfig                              │  │
                              │  │    └─► ReceiverOptions (topic: program-events)    │  │
                              │  │                                                   │  │
                              │  │  SubscriptionEventConsumer                        │  │
                              │  │    └─► receiver.receive()          (non-blocking) │  │
                              │  │          └─► doOnNext(record → log + ack)         │  │
                              │  │          └─► doOnError(e → log)                   │  │
                              │  └───────────────────────────────────────────────────┘  │
                              └──────────────────────────────────────────────────────────┘

(*) KafkaSender en program-service: pendiente de implementación
```

---

## Flujo de un evento

```
1. Cliente hace POST /programs?name=Jazz Morning
        │
        ▼
2. ProgramController crea Program(uuid, "Jazz Morning")
        │
        ▼
3. KafkaSender (*) publica en topic "program-events"
   key: <uuid>   value: "Jazz Morning"
        │
        ▼
4. Kafka KRaft persiste el mensaje en las 3 particiones
        │
        ▼
5. SubscriptionEventConsumer recibe el ReceiverRecord
   (reactor thread, non-blocking)
        │
        ▼
6. log.info("Received event: key=<uuid> value=Jazz Morning")
   record.receiverOffset().acknowledge()
```

---

## Stack por capa

```
┌─────────────────────────────────────────────────────┐
│  API Layer                                          │
│  Spring WebFlux — @RestController                   │
│  program-service :8081                              │
├─────────────────────────────────────────────────────┤
│  Messaging Layer                                    │
│  Apache Kafka KRaft 3.6 (via cp-kafka 7.6.0)        │
│  reactor-kafka 1.3.23 (non-blocking consumer)       │
├─────────────────────────────────────────────────────┤
│  Resilience Layer (pendiente)                       │
│  Resilience4J — CircuitBreaker                      │
│  Spring Cloud 2023.0.0                              │
├─────────────────────────────────────────────────────┤
│  Infrastructure Layer                               │
│  LXD/LXC container — Ubuntu 24.04                  │
│  Docker + docker-compose                            │
└─────────────────────────────────────────────────────┘
```

---

## Puertos

| Servicio | Puerto | Acceso |
|---|---|---|
| program-service | 8081 | host |
| subscription-service | 8082 | host |
| Kafka (interno) | 9092 | dentro del contenedor |
| Kafka (externo) | 29092 | host → contenedor (proxy LXC) |
| Kafka Controller | 9093 | interno KRaft, no expuesto |
| Remote JVM Debug | 5005 | host → contenedor (proxy LXC, opcional) |

---

## Servicios pendientes de implementación

| Servicio | Responsabilidad |
|---|---|
| `email-service` | Consume `program-events`, envía notificaciones |
| `order-service` | Gestiona órdenes de suscripción |
| `gateway` | API Gateway (Spring Cloud Gateway) |
