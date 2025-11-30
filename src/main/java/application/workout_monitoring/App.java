package application.workout_monitoring;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class App {
    private static MqttAsyncClient myClient;

	public static void main( String[] args ) throws MqttException{
        myClient = new MqttAsyncClient("tcp://localhost:1883", UUID.randomUUID().toString());
        MyCallback myCallback = new MyCallback();
        myClient.setCallback(myCallback);
        
        IMqttToken token = myClient.connect();
        token.waitForCompletion();
        
        myClient.subscribe("/teste/1", 0);
    }
}
