package sensors;

import org.eclipse.paho.client.mqttv3.*;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

class CaloriesConfig {
    public String brokerUrl = "tcp://localhost:1883";
    public String topic = "cardio/sensor/calories";
    public int publishIntervalMs = 5000; // envia a cada 5 segundos
    public double baseCaloriesPerMin = 10; // gasto médio por minuto
    public double noiseRange = 2.0; // variação de +-2 kcal/min
    public boolean retained = false;
}

public class CaloriesSensorPublisher {
    private final CaloriesConfig config;
    private final Random random = new Random();
    private MqttAsyncClient client;
    private String sensorId;

    private double totalCalories = 0.0;

    public CaloriesSensorPublisher(CaloriesConfig config) {
        this.config = config;
        this.sensorId = "cal-sensor-" + UUID.randomUUID();
    }

    public void start() throws Exception {
        connect();
        System.out.println("[CaloriesSensor] Online | SensorId=" + sensorId);

        while (true) {
            double calories = generateCaloriesIncrement();
            totalCalories += calories;

            String payload = buildJson(totalCalories);

            client.publish(
                config.topic,
                payload.getBytes(),
                1,
                config.retained
            );

            System.out.println("[CaloriesSensor] Sent → TotalCalories=" +
                    String.format(java.util.Locale.US, "%.2f", totalCalories) +
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

    private double generateCaloriesIncrement() {
        // gasta caloriasPorMin convertidas para caloriasPorSegundo
        double noise = (random.nextDouble() * 2 * config.noiseRange) - config.noiseRange;
        double caloriesPerMin = config.baseCaloriesPerMin + noise;

        double caloriesPerSec = caloriesPerMin / 60.0;

        // multiplica pelo intervalo configurado
        return caloriesPerSec * (config.publishIntervalMs / 1000.0);
    }

    private String buildJson(double totalCalories) {
        return "{" +
                "\"sensorId\":\"" + sensorId + "\"," +
                "\"calories\":" + String.format(java.util.Locale.US, "%.2f", totalCalories) + "," +
                "\"ts\":\"" + Instant.now() + "\"" +
                "}";
    }

    public static void main(String[] args) throws Exception {
        CaloriesConfig cfg = new CaloriesConfig();
        new CaloriesSensorPublisher(cfg).start();
    }
}
