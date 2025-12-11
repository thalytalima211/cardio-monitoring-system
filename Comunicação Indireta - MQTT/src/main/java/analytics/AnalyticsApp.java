package analytics;

import org.eclipse.paho.client.mqttv3.*;
import utils.Config;
import utils.GetIP;

import java.net.InetAddress;
import java.util.UUID;

public class AnalyticsApp {
    private static final String BROKER = "tcp://" + Config.get("mqtt.broker") + ":" + Config.get("mqtt.port");
    private static final String CLIENT_ID = "analytics-" + UUID.randomUUID().toString();

    public static void main(String[] args) throws Exception {
        String localIp = GetIP.getMyIp();

        System.out.println("[AnalyticsApp] -----------------------------");
        System.out.println("[AnalyticsApp] Iniciando Analytics…");
        System.out.println("[AnalyticsApp] Rodando nesta máquina (IP local): " + localIp);
        System.out.println("[AnalyticsApp] Recebendo dados vindo do broker (IP): " + BROKER);
        System.out.println("[AnalyticsApp] -----------------------------");

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
