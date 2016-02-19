package jot.chapter4.mqtt;

import java.util.Date;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class HelloMqttSubscriber {

    private final static String CLIENT_ID = "simplemqttsubscriber";
    private final static String TOPIC = "Sports";

    private String url;
    private MqttClient client;

    public HelloMqttSubscriber() {
        url = System.getProperty("mqtt.server");
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

        try {
            MqttConnectOptions conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(true);

            client = new MqttClient(url, CLIENT_ID, dataStore);
            client.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println(
                            String.format("Arrived - [%s] message: %s ",
                                    new Date(), message));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }

                @Override
                public void connectionLost(Throwable cause) {
                    cause.printStackTrace();
                    System.exit(-1);
                }
            });

            client.connect(conOpt);
        } catch (MqttException e) {
            System.exit(1);
        }
    }

    public void subscribe() throws MqttException {
        client.subscribe(TOPIC);
    }

    public void close() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
                client = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        HelloMqttSubscriber subscriber = new HelloMqttSubscriber();
        try {
            subscriber.subscribe();

            Scanner input = new Scanner(System.in);
            System.out.println("press enter key to quit: ");
            input.nextLine();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        subscriber.close();
    }
}
