# Infrastructure Setup Guide — LXD + Docker + Kafka KRaft

Paso a paso para levantar el entorno de desarrollo completo desde cero en Ubuntu 24.04.

---

## Prerequisitos

```bash
# Verificar que LXD está instalado
lxd --version

# Si no está instalado:
sudo snap install lxd
sudo lxd init --minimal
sudo usermod -aG lxd $USER
newgrp lxd
```

---

## Paso 1 — Crear el contenedor LXC

```bash
lxc launch ubuntu:24.04 radio-dev

lxc config set radio-dev security.nesting true
lxc config set radio-dev limits.cpu 4
lxc config set radio-dev limits.memory 6GB

lxc restart radio-dev
```

---

## Paso 2 — Exponer puertos de Kafka al host

```bash
# Listener interno (servicios dentro del contenedor)
lxc config device add radio-dev kafka-internal proxy \
  listen=tcp:0.0.0.0:9092 connect=tcp:127.0.0.1:9092

# Listener externo (IntelliJ y servicios en el host)
lxc config device add radio-dev kafka-external proxy \
  listen=tcp:0.0.0.0:29092 connect=tcp:127.0.0.1:29092
```

---

## Paso 3 — Instalar Docker dentro del contenedor

```bash
lxc exec radio-dev -- bash -c "
  apt-get update && apt-get install -y ca-certificates curl gnupg &&
  install -m 0755 -d /etc/apt/keyrings &&
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg &&
  echo 'deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu noble stable' \
    > /etc/apt/sources.list.d/docker.list &&
  apt-get update && apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin &&
  systemctl enable --now docker
"
```

Verificar:

```bash
lxc exec radio-dev -- docker version
```

---

## Paso 4 — Instalar Java 17 en el contenedor (opcional)

Solo necesario si vas a ejecutar los JARs dentro del contenedor (Opción B).

```bash
lxc exec radio-dev -- bash -c "
  apt-get install -y openjdk-17-jdk maven &&
  echo 'JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> /etc/environment
"
```

---

## Paso 5 — Obtener la IP del contenedor

```bash
lxc list radio-dev --format csv -c 4 | head -1
# Ejemplo: 10.75.120.45
```

Guardá este valor — lo vas a usar como `<CONTAINER_IP>` en los pasos siguientes.

---

## Paso 6 — Configurar el docker-compose de Kafka

Editá `docker/docker-compose.yml` y reemplazá `<CONTAINER_IP>` con la IP del paso anterior en la línea:

```yaml
KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:9092,EXTERNAL://<CONTAINER_IP>:29092
```

> Si necesitás un `CLUSTER_ID` nuevo (cluster limpio):
> ```bash
> lxc exec radio-dev -- docker run --rm confluentinc/cp-kafka:7.6.0 kafka-storage random-uuid
> ```
> Reemplazalo en el compose antes de continuar.

---

## Paso 7 — Levantar Kafka KRaft

```bash
# Copiar el compose al contenedor
lxc file push docker/docker-compose.yml radio-dev/root/docker-compose.yml

# Levantar
lxc exec radio-dev -- docker compose -f /root/docker-compose.yml up -d

# Verificar
lxc exec radio-dev -- docker compose -f /root/docker-compose.yml ps
```

Salida esperada:

```
NAME          STATUS    PORTS
kafka-kraft   running   0.0.0.0:9092->9092/tcp, 0.0.0.0:29092->29092/tcp
```

---

## Paso 8 — Crear el topic `program-events`

```bash
lxc exec radio-dev -- docker exec kafka-kraft \
  kafka-topics --create \
  --topic program-events \
  --partitions 3 \
  --replication-factor 1 \
  --bootstrap-server localhost:9092

# Verificar
lxc exec radio-dev -- docker exec kafka-kraft \
  kafka-topics --list --bootstrap-server localhost:9092
```

---

## Paso 9 — Montar el proyecto en el contenedor (opcional)

Útil para compilar y ejecutar los servicios desde adentro sin copiar JARs manualmente.

```bash
lxc config device add radio-dev project-src disk \
  source=/home/rocio/radio-station-microservices \
  path=/opt/radio-station
```

---

## Paso 10 — Compilar los servicios

**Desde el host:**

```bash
cd /home/rocio/radio-station-microservices
mvn clean package -DskipTests
```

**Desde el contenedor** (si montaste el proyecto en el paso anterior):

```bash
lxc exec radio-dev -- mvn -f /opt/radio-station/pom.xml clean package -DskipTests
```

---

## Paso 11 — Ejecutar los servicios

**Opción A — Desde el host** (conectando a Kafka vía IP del contenedor):

```bash
# Terminal 1
KAFKA_BOOTSTRAP_SERVERS=<CONTAINER_IP>:29092 \
  java -jar services/program-service/target/program-service-1.0.0-SNAPSHOT.jar

# Terminal 2
KAFKA_BOOTSTRAP_SERVERS=<CONTAINER_IP>:29092 \
  java -jar services/subscription-service/target/subscription-service-1.0.0-SNAPSHOT.jar
```

**Opción B — Desde el contenedor** (usando el listener interno):

```bash
# Terminal 1
lxc exec radio-dev -- bash -c "
  KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  java -jar /opt/radio-station/services/program-service/target/program-service-1.0.0-SNAPSHOT.jar
"

# Terminal 2
lxc exec radio-dev -- bash -c "
  KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  java -jar /opt/radio-station/services/subscription-service/target/subscription-service-1.0.0-SNAPSHOT.jar
"
```

---

## Paso 12 — Verificar que todo funciona

```bash
# Health check
curl http://localhost:8081/programs/ping
# → pong

# Crear un programa
curl -X POST "http://localhost:8081/programs?name=Jazz+Morning"
# → {"id":"<uuid>","name":"Jazz Morning"}
```

Revisá los logs del `subscription-service` para confirmar que el consumer reactivo está activo.

---

## Paso 13 — Configurar IntelliJ IDEA

1. **Variables de entorno** en cada Run Configuration:
   ```
   KAFKA_BOOTSTRAP_SERVERS=<CONTAINER_IP>:29092
   ```

2. **Remote JVM Debug** (para depurar servicios corriendo en el contenedor):

   ```bash
   # Exponer puerto de debug
   lxc config device add radio-dev debug-sub proxy \
     listen=tcp:0.0.0.0:5005 connect=tcp:127.0.0.1:5005

   # Ejecutar con agente de debug
   lxc exec radio-dev -- bash -c "
     java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
     -jar /opt/radio-station/services/subscription-service/target/subscription-service-1.0.0-SNAPSHOT.jar
   "
   ```

   En IntelliJ: `Run → Edit Configurations → + → Remote JVM Debug`
   - Host: `localhost`
   - Port: `5005`

3. **Kafka plugin** (Big Data Tools o Kafka Tool):
   - Bootstrap server: `<CONTAINER_IP>:29092`
   - Security: `PLAINTEXT`

---

## Referencia rápida de comandos LXC

```bash
lxc list                          # ver todos los contenedores
lxc start radio-dev               # iniciar
lxc stop radio-dev                # detener
lxc exec radio-dev -- bash        # entrar al contenedor
lxc file push <local> radio-dev/<remoto>   # copiar archivo al contenedor
lxc config device list radio-dev  # ver dispositivos/proxies configurados
```
