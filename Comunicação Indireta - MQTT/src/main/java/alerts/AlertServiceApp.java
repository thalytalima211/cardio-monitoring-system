package alerts;

import org.eclipse.paho.client.mqttv3.*;
import java.util.UUID;

public class AlertServiceApp {
    private static final String BROKER = "tcp://localhost:1883";
    private static final String CLIENT_ID = "alert-service-" + UUID.randomUUID().toString();

    public static void main(String[] args) throws Exception {
        System.out.println("[AlertService] Iniciando...");
        MqttAsyncClient client = new MqttAsyncClient(BROKER, CLIENT_ID);
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);

        IMqttToken token = client.connect(opts);
        token.waitForCompletion();
        System.out.println("[AlertService] Conectado ao broker.");

        client.setCallback(new AlertCallback());
        client.subscribe("cardio/alert/#", 1);

        System.out.println("[AlertService] Subscribed em cardio/alert/#");
        System.out.println("[AlertService] Aguardando alertas...");
        while (true) {
            Thread.sleep(1000);
        }
    }
}
