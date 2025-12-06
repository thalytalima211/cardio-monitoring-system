# 🏃‍♂️ **Workout Monitoring System**

Sistema distribuído para monitoramento de métricas físicas em tempo real usando sensores simulados e comunicação via **MQTT**.

## 📌 **Visão Geral**

Este projeto implementa um ecossistema distribuído onde múltiplos sensores de exercícios físicos publicam dados em tópicos MQTT. Um módulo assinante coleta essas informações para posterior análise, visualização ou integração com sistemas externos.

A proposta é demonstrar, na prática, como comunicação assíncrona leve (MQTT) pode orquestrar sensores independentes — simulando um ambiente real de monitoramento esportivo.

---

# **Arquitetura**

### **1. Sensores (Publicadores MQTT)**

Cada sensor roda como um processo independente e simula leituras com ruído, picos e variações naturais:

* **HeartbeatSensorPublisher**
  Publica BPM com variação, incluindo picos ocasionais.

* **CaloriesSensorPublisher**
  Simula gasto calórico cumulativo em tempo real.

* **PaceSensorPublisher**
  Envia velocidade, pace e distância total percorrida.

Todos publicam com:

* QoS 1
* Intervalo de envio configurável (default 5s)
* Mensagens em JSON
* ID único por sensor

---

### **2. Módulo Subscriber – Workout Monitoring**

Localizado em `application.workout_monitoring`.

Esse módulo:

* Conecta ao broker MQTT.
* Subscreve um tópico específico (atualmente `/teste/1`).
* Recebe mensagens via callback.
* Imprime os dados recebidos.

> **Ajuste futuro necessário:** Subscribes apropriados dos sensores:

* `cardio/sensor/bpm`
* `cardio/sensor/calories`
* `cardio/sensor/pace_speed`

---

# **Tópicos Utilizados**

| Sensor     | Tópico                     | Exemplo de Payload                                  |
| ---------- | -------------------------- | --------------------------------------------------- |
| BPM        | `cardio/sensor/bpm`        | `{ "sensorId":"xx", "bpm":145, "ts":"..." }`        |
| Calorias   | `cardio/sensor/calories`   | `{ "sensorId":"xx", "calories":52.21, "ts":"..." }` |
| Pace/Speed | `cardio/sensor/pace_speed` | `{ "sensorId":"xx", "speed_kmh":9.8, ... }`         |

---

# **Como Executar**

### **1. Subir o broker Mosquitto**

```sh
mosquitto
```

### **2. Iniciar Sensores (cada um em processo separado)**

```sh
java sensors.HeartbeatSensorPublisher
java sensors.CaloriesSensorPublisher
java sensors.PaceSensorPublisher
```

### **3. Iniciar o Subscriber**

```sh
java application.workout_monitoring.App
```

---

# **Tecnologias**

* **Java 17+**
* **MQTT** (Eclipse Paho)
* **Mosquitto Broker**
* Threads, JSON manual, geração de ruído e simulações probabilísticas.

---

# **Funcionalidades**

* [x] Sensores completamente funcionais
* [x] Publicação contínua MQTT com dados realistas
* [x] Estrutura modular
* [x] Subscriber funcional conectado ao broker
* [x] Build JSON manual
* [x] Configurações independentes por sensor
* [ ] Subscribes corretos para **cada** tópico real do projeto.
* [ ] Tratamento dos payloads JSON recebidos.
* [ ] Impressão organizada ou agregação de dados.
* [ ] Exibir BPM, calorias e pace em tempo real.
* [ ] Detectar anomalias (picos extremos, dados faltantes, valores fora do range).
* [ ] Armazenar logs para análise posterior.

---

# **Objetivo Didático**

Mostrar, na prática, como protocolos leves e assíncronos como **MQTT** possibilitam a criação de sistemas distribuídos responsivos — simulando um ambiente esportivo inteligente com sensores independentes e comunicação de baixa latência.