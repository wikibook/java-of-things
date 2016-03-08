package jot.chapter4.mqtt;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
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
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttEchoReceiver {

    public static final String TOPIC_PREFIX = "jot/chapter4/mqtt/";
    public static final String TOPIC_ECHO = TOPIC_PREFIX + "+/echo";
    public static final String TOPIC_RESPONSE = 
            TOPIC_PREFIX + "%s/response";

    private String url;
    private String clientId;
    private MqttClient client;

    public MqttEchoReceiver() throws Exception {
        url = System.getProperty("mqtt.server");
       String ipAddress = getLocalIPAddress();
        if (ipAddress == null) {
            throw new Exception("Cannot find a ip address.");
        }

        System.out.println("Used IP Address: " + ipAddress);
        clientId = ipAddress.replace('.', '_');
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = 
                new MqttDefaultFilePersistence(tmpDir);

        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);

        client = new MqttClient(url, clientId, dataStore);
        client.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(String topic, 
                    MqttMessage message) throws Exception {
                String[] clientInfo
                        = topic.substring(TOPIC_PREFIX.length())
                        .split("/");
                JsonObject json = Json.createObjectBuilder()
                        .add("clientId", clientInfo[0])
                        .add("message", message.toString())
                        .build();
                publish(clientInfo[0], json.toString(), 1);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    Logger.getLogger(MqttEchoReceiver.class.getName())
                            .log(Level.INFO, String.format(
                                    "Delivered - [%s] message: %s ",
                                    new Date(), token.getMessage()));
                } catch (MqttException ignored) {
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Logger.getLogger(MqttEchoReceiver.class.getName())
                        .log(Level.SEVERE, null, cause);
                System.exit(-1);
            }
        });

        client.connect(conOpt);
        client.subscribe(TOPIC_ECHO);
    }

    public void publish(String clientId, 
            String payload, int qos) throws MqttException {
        MqttTopic topic = 
                client.getTopic(String.format(TOPIC_RESPONSE, clientId));
        topic.publish(payload.getBytes(), qos, false);
    }

    public void close() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
                client = null;
            } catch (MqttException ex) {
                Logger.getLogger(MqttEchoReceiver.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getLocalIPAddress() throws SocketException {
        String ipAddress = null;
        Enumeration<NetworkInterface> networkList = 
                NetworkInterface.getNetworkInterfaces();
        NetworkInterface ni;
        InetAddress address;
        while (networkList.hasMoreElements()) {
            ni = networkList.nextElement();
            if (!ni.getName().equals("eth0")) {
                continue;
            }
            Enumeration<InetAddress> addresses = 
                    ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                address = addresses.nextElement();
                if(address instanceof Inet4Address){
                    ipAddress = address.getHostAddress();
                    break;
                }            
            }

            if (ipAddress != null) {
                break;
            }
        }

        return ipAddress;
    }

    public static void main(String[] args) {
        try {
            MqttEchoReceiver server = new MqttEchoReceiver();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    server.close();
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(MqttEchoReceiver.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
