package alerts;

import org.eclipse.paho.client.mqttv3.*;
import java.nio.charset.StandardCharsets;

public class AlertCallback implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("[AlertService] Conexão perdida: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        // formata e imprime alerta
        System.out.println("\n===================== ALERTA RECEBIDO =====================");
        System.out.println("Tópico: " + topic);
        System.out.println("Mensagem: " + payload);
        System.out.println("===========================================================\n");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) { }
}
