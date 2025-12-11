package alerts;

import org.eclipse.paho.client.mqttv3.*;
import utils.Config;
import utils.GetIP;

import java.net.InetAddress;
import java.util.UUID;

public class AlertServiceApp {
    private static final String BROKER = "tcp://" + Config.get("mqtt.broker") + ":" + Config.get("mqtt.port");
    private static final String CLIENT_ID = "alert-service-" + UUID.randomUUID().toString();

    public static void main(String[] args) throws Exception {
        String localIp = GetIP.getMyIp();

        System.out.println("[AlertService] -----------------------------");
        System.out.println("[AlertService] Iniciando Alerts…");
        System.out.println("[AlertService] Rodando nesta máquina (IP local): " + localIp);
        System.out.println("[AlertService] Recebendo dados vindo do broker (IP): " + BROKER);
        System.out.println("[AlertService] -----------------------------");

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
