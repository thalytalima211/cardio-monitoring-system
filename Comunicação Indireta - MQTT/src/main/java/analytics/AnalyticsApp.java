package analytics;

import org.eclipse.paho.client.mqttv3.*;
import java.util.UUID;

public class AnalyticsApp {
    private static final String BROKER = "tcp://localhost:1883";
    private static final String CLIENT_ID = "analytics-" + UUID.randomUUID().toString();

    public static void main(String[] args) throws Exception {
        System.out.println("[AnalyticsApp] Iniciando...");

        MqttAsyncClient client = new MqttAsyncClient(BROKER, CLIENT_ID);
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);

        IMqttToken token = client.connect(opts);
        token.waitForCompletion();
        System.out.println("[AnalyticsApp] Conectado ao broker: " + BROKER);

        // callback deve publicar usando o mesmo client
        AnalyticsCallback callback = new AnalyticsCallback(client);
        client.setCallback(callback);

        // inscrever-se nos tópicos de sensores
        client.subscribe("cardio/sensor/bpm", 1);
        client.subscribe("cardio/sensor/calories", 1);
        client.subscribe("cardio/sensor/pace_speed", 1);

        System.out.println("[AnalyticsApp] Subscribed em cardio/sensor/#");
        System.out.println("[AnalyticsApp] Aguardando mensagens...");
        // manter a aplicação viva
        while (true) {
            Thread.sleep(1000);
        }
    }
}
