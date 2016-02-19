package jot.chapter4.mqtt.thing;

import java.io.StringReader;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttConsole {

    public static final String TOPIC_PREFIX = "jot/chapter4/mqtt/thing/";
    public static final String TOPIC_COMMAND = TOPIC_PREFIX + "%s/%s/command";
    public static final String TOPIC_RESULT = TOPIC_PREFIX + "%s/result";
    public static final String TOPIC_BROADCAST = TOPIC_PREFIX + "%s/broadcast";

    private String url;
    private String clientId;
    private MqttClient client;
    private String commandTopic;
    private String resultTopic;
    private String broadcastTopic;
    private BigDataHandler bigData;

    public MqttConsole(String clientId, String handlerId, 
            BigDataHandler bigData) throws MqttException {
        this.clientId = clientId;
        this.bigData = bigData;
        url = System.getProperty("mqtt.server");
        commandTopic = String.format(TOPIC_COMMAND, 
                clientId, handlerId);
        resultTopic = String.format(TOPIC_RESULT, clientId);
        broadcastTopic = String.format(TOPIC_BROADCAST, handlerId);

        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = 
                new MqttDefaultFilePersistence(tmpDir);

        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);

        client = new MqttClient(url, clientId, dataStore);
        client.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.endsWith("broadcast")){
                    StringReader in = new StringReader(message.toString());
                    JsonObject json = Json.createReader(in).readObject();
                    String broadcastType = json.getString("type");
                    if(broadcastType.equals(ChangeOfValue.TYPE)){
                        ChangeOfValue cov = new ChangeOfValue(json);
                        bigData.saveBigData(cov);
                    }else{
                        System.out.println("Received a breoadcast: " + message);
                    }
                }else{
                    System.out.println("Result: " + message);
                    System.out.print("input command or 'q'(quit): ");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    Logger.getLogger(MqttConsole.class.getName())
                            .log(Level.INFO, String.format(
                                    "Delivered - [%s] message: %s ",
                                    new Date(), token.getMessage()));
                } catch (MqttException ignored) {
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Logger.getLogger(MqttConsole.class.getName())
                        .log(Level.SEVERE, null, cause);
                System.exit(-1);
            }
        });

        client.connect(conOpt);
        client.subscribe(resultTopic);
        client.subscribe(broadcastTopic);
    }

    public void publish(String payload, int qos) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        client.publish(commandTopic, message);
    }

    public void close() throws MqttException {
        if (client != null) {
            client.disconnect();
            client.close();
            client = null;
        }
    }
    
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Input ID: ");
        String clientId = input.nextLine();
        System.out.print("Input the IP Address of thing: ");
        String handlerId = input.nextLine().replace('.', '_');
        System.out.println("MqttConsole connecting...");
        try {
            BigDataHandler bigData = new BigDataHandler();
            MqttConsole console = 
                    new MqttConsole(clientId, handlerId, bigData);
            System.out.print("input command or 'q'(quit): ");
            for (String line = input.nextLine();
                    !line.equals("q");
                    line = input.nextLine()) {
                
                if(line.trim().length() == 0)
                    continue;
                
                switch(line){
                    case "display":
                        bigData.displayBigData();
                        System.out.print("input command or 'q'(quit): ");
                        break;
                    case "clear":
                        bigData.clearBigData();
                        System.out.print("input command or 'q'(quit): ");
                        break;
                    default:
                        console.publish(line, 1);
                        System.out.print("Waiting result....");
                }
            }
            console.close();
        } catch (MqttException ex) {
            Logger.getLogger(MqttConsole.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
