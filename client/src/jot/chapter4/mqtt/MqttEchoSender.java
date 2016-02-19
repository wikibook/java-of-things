package jot.chapter4.mqtt;

import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttEchoSender {

    public static final String TOPIC_PREFIX = "jot/chapter4/mqtt/";
    public static final String TOPIC_ECHO = TOPIC_PREFIX + "%s/echo";
    public static final String TOPIC_RESPONSE = TOPIC_PREFIX + "%s/response";

    private String url;
    private String clientId;
    private MqttClient client;
    private String echoTopic;
    private String responseTopic;

    public MqttEchoSender(String clientId) throws MqttException {
        this.clientId = clientId;
        url = System.getProperty("mqtt.server");
        echoTopic = String.format(TOPIC_ECHO, clientId);
        responseTopic = String.format(TOPIC_RESPONSE, clientId);

        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);

        client = new MqttClient(url, clientId, dataStore);
        client.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println(String.format("Echo -> %s", message));
                System.out.print("input message or 'q'(quit): ");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    Logger.getLogger(MqttEchoSender.class.getName())
                            .log(Level.INFO, String.format(
                                    "Delivered - [%s] message: %s ",
                                    new Date(), token.getMessage()));
                } catch (MqttException ignored) {
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Logger.getLogger(MqttEchoSender.class.getName())
                        .log(Level.SEVERE, null, cause);
                System.exit(-1);
            }
        });

        client.connect(conOpt);
        client.subscribe(responseTopic);
    }

    public void publish(String payload, int qos) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        client.publish(echoTopic, message);
    }

    public void close() throws MqttException {
        if (client != null) {
            client.disconnect();
            client.close();
            client = null;
        }
    }

    public static void main(String[] args) {
//        Scanner input = new Scanner(System.in);
         Scanner input = new Scanner(System.in, "MS949"); // 한글
        System.out.print("Input ID: ");
        String clientId = input.nextLine();
        System.out.println("MqttClient connecting...");
        try {
            MqttEchoSender client = new MqttEchoSender(clientId);
            System.out.print("input message or 'q'(quit): ");
            for (String line = input.nextLine();
                    !line.equals("q");
                    line = input.nextLine()) {
                client.publish(line, 1);
                System.out.print("Waiting echo....");
            }
            client.close();
        } catch (MqttException ex) {
            Logger.getLogger(MqttEchoSender.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
