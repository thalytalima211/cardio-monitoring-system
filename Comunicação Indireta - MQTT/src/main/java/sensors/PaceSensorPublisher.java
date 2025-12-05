package sensors;

import org.eclipse.paho.client.mqttv3.*;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

class PaceSpeedConfig {
    public String brokerUrl = "tcp://localhost:1883";
    public String topic = "cardio/sensor/pace_speed";
    public int publishIntervalMs = 5000; 
    public double baseSpeedKmH = 9.5;
    public double speedNoiseRange = 1;
    public boolean retained = false;
}

public class PaceSensorPublisher {
    private final PaceSpeedConfig config;
    private final Random random = new Random();
    private MqttAsyncClient client;
    private String sensorId;
    private double distanceKm = 0.0;

    public PaceSensorPublisher(PaceSpeedConfig config) {
        this.config = config;
        this.sensorId = "pace-sensor-" + UUID.randomUUID();
    }

    public void start() throws Exception {
        connect();
        System.out.println("[PaceSensor] Online | SensorId=" + sensorId);

        while (true) {
            double speed = generateSpeed();
            double pace = convertSpeedToPace(speed);
            updateDistance(speed);

            String payload = buildJson(speed, pace, distanceKm);

            client.publish(
                config.topic,
                payload.getBytes(),
                1,
                config.retained
            );

            System.out.println("[PaceSensor] Sent → speed=" + String.format("%.2f", speed) +
                    " km/h | pace=" + formatPace(pace) +
                    " | distance=" + String.format("%.3f", distanceKm) + " km | ts=" + Instant.now());

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

    private double generateSpeed() {
        double noise = (random.nextDouble() * 2 * config.speedNoiseRange) - config.speedNoiseRange;
        double speed = config.baseSpeedKmH + noise;

        if (random.nextDouble() < 0.02) {
            speed += 3 + random.nextDouble() * 3; 
        }

        return Math.max(3.0, speed);
    }

    private double convertSpeedToPace(double speedKmH) {
        return 60.0 / speedKmH;
    }

    private String formatPace(double pace) {
        int minutes = (int) pace;
        int seconds = (int) Math.round((pace - minutes) * 60);
        return minutes + ":" + String.format("%02d", seconds) + " min/km";
    }

    private void updateDistance(double speedKmH) {
        double hours = config.publishIntervalMs / 3600000.0;
        distanceKm += speedKmH * hours;
    }

    private String buildJson(double speedKmH, double pace, double distanceKm) {
        return "{" +
                "\"sensorId\":\"" + sensorId + "\"," +
                "\"speed_kmh\":" + String.format("%.2f", speedKmH) + "," +
                "\"pace_min_km\":" + String.format("%.2f", pace) + "," +
                "\"distance_km\":" + String.format("%.3f", distanceKm) + "," +
                "\"ts\":\"" + Instant.now() + "\"" +
                "}";
    }

    public static void main(String[] args) throws Exception {
        PaceSpeedConfig cfg = new PaceSpeedConfig();
        new PaceSensorPublisher(cfg).start();
    }
}