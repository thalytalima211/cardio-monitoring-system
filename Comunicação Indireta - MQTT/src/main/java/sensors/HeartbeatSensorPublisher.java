package sensors;

import org.eclipse.paho.client.mqttv3.*;
import utils.Config;
import utils.GetIP;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

class SensorConfig {
    public String brokerUrl = "tcp://" + Config.get("mqtt.broker") + ":" + Config.get("mqtt.port");
    public String topic = "cardio/sensor/bpm";
    public int publishIntervalMs = 5000;
    public int baseBpm = 140;
    public int noiseRange = 25;
    public boolean simulatePeaks = true;
    public boolean retained = false;
}

public class HeartbeatSensorPublisher {
    private final SensorConfig config;
    private final Random random = new Random();
    private MqttAsyncClient client;
    private String sensorId;

    public HeartbeatSensorPublisher(SensorConfig config) {
        this.config = config;
        this.sensorId = "sensor-" + UUID.randomUUID();
    }

    public void start() throws Exception {
        connect();
        String localIp = GetIP.getMyIp();

        System.out.println("[HeartbeatSensor] -----------------------------");
        System.out.println("[HeartbeatSensor] Iniciando Sensor de batimentos…");
        System.out.println("[HeartbeatSensor] Rodando nesta máquina (IP local): " + localIp);
        System.out.println("[HeartbeatSensor] Enviando dados para broker (IP): " + Config.get("mqtt.broker"));
        System.out.println("[HeartbeatSensor] Online | SensorId=" + sensorId);
        System.out.println("[HeartbeatSensor] -----------------------------");

        while (true) {
            int bpm = generateBpm();
            String payload = buildJson(bpm);

            client.publish(
                config.topic,
                payload.getBytes(),
                1,
                config.retained
            );

            System.out.println("[Sensor] Sent → BPM=" + bpm +
                    " | topic=" + config.topic +
                    " | ts=" + Instant.now());

            Thread.sleep(config.publishIntervalMs);
        }
    }

    private void connect() throws Exception {
        client = new MqttAsyncClient(config.brokerUrl, sensorId);

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);

        IMqttToken token = client.connect(opts);
        token.waitForCompletion();
    }

    private int generateBpm() {
        int noise = random.nextInt(config.noiseRange * 2) - config.noiseRange;
        int bpm = config.baseBpm + noise;

        if (config.simulatePeaks && random.nextDouble() < 0.015) {
            return 175+ random.nextInt(15); 
        }
        return Math.max(40, bpm);
    }

    private String buildJson(int bpm) {
        return "{" +
                "\"sensorId\":\"" + sensorId + "\"," +
                "\"bpm\":" + bpm + "," +
                "\"ts\":\"" + Instant.now() + "\"" +
                "}";
    }

    public static void main(String[] args) throws Exception {
        SensorConfig cfg = new SensorConfig();
        new HeartbeatSensorPublisher(cfg).start();
    }
}
