package jot.chapter4.mqtt;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class HelloMqttPublisher {

    private final static String CLIENT_ID = "simplemqttpublisher";
    private final static String TOPIC = "Sports";

    private String url;
    private MqttClient client;

    public HelloMqttPublisher() {
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

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        System.out.println(
                                String.format("Delivered - [%s] message: %s ",
                                        new Date(), token.getMessage()));
                    } catch (MqttException e) {
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.exit(-1);
                }
            });

            client.connect(conOpt);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void publish(String payload, int qos) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        client.publish(TOPIC, message);
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
        HelloMqttPublisher publisher = new HelloMqttPublisher();

        try {
            for (int i = 0; i < 3; i++) {
                System.out.println("Publishing 'Hello QoS " + i + "'");
                publisher.publish("Hello QoS " + i, i);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        publisher.close();
    }
}
