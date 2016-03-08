package jot.chapter4.mqtt.thing;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import jot.chapter3.thing.Commander;
import jot.chapter3.thing.Point;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttConsoleHandler implements Observer {

    public static final String TOPIC_PREFIX = "jot/chapter4/mqtt/thing/";
    public static final String TOPIC_COMMAND = TOPIC_PREFIX + "+/%s/command";
    public static final String TOPIC_RESULT = TOPIC_PREFIX + "%s/result";
    public static final String TOPIC_BROADCAST = TOPIC_PREFIX + "%s/broadcast";

    private ExecutorService commandThread = Executors.newCachedThreadPool();
    private String url;
    private String handlerId;
    private MqttClient client;
    private String commandTopic;
    private String broadcastTopic;

    public MqttConsoleHandler() throws Exception {
        url = System.getProperty("mqtt.server");
        String ipAddress = getLocalIPAddress();
        if (ipAddress == null) {
            throw new Exception("Cannot find a ip address.");
        }

        System.out.println("Used IP Address: " + ipAddress);
        handlerId = ipAddress.replace('.', '_');
        commandTopic = String.format(TOPIC_COMMAND, handlerId);
        broadcastTopic = String.format(TOPIC_BROADCAST, handlerId);

        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore
                = new MqttDefaultFilePersistence(tmpDir);

        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);

        client = new MqttClient(url, handlerId, dataStore);
        client.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String[] clientInfo
                        = topic.substring(TOPIC_PREFIX.length())
                        .split("/");
                final String commandStr = message.toString();
                commandThread.submit(new Runnable() {

                    @Override
                    public void run() {
                        String[] command = commandStr.split(" ");
                        String result;
                        try {
                            result = Commander.getInstance().execute(command);
                            result = result == null ? "Success" : result;
                        } catch (Throwable ex) {
                            result = "Exception happend: " + ex.getMessage();
                        }

                        try {
                            publish(clientInfo[0], result, 1);
                        } catch (MqttException ex) {
                            Logger.getLogger(
                                    MqttConsoleHandler.class.getName())
                                    .log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    Logger.getLogger(MqttConsoleHandler.class.getName())
                            .log(Level.INFO, String.format(
                                    "Delivered - [%s] message: %s ",
                                    new Date(), token.getMessage()));
                } catch (MqttException ignored) {
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Logger.getLogger(MqttConsoleHandler.class.getName())
                        .log(Level.SEVERE, null, cause);
                System.exit(-1);
            }
        });

        client.connect(conOpt);
        client.subscribe(commandTopic);
    }

    public void publish(String clientId, String payload, int qos)
            throws MqttException {
        MqttTopic topic
                = client.getTopic(String.format(TOPIC_RESULT, clientId));
        topic.publish(payload.getBytes(), qos, false);
    }

    public void broadcast(String payload)
            throws MqttException {
        MqttTopic topic
                = client.getTopic(broadcastTopic);
        topic.publish(payload.getBytes(), 0, false);
    }

    public void close() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
                client = null;
            } catch (MqttException ex) {
                Logger.getLogger(MqttConsoleHandler.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void update(Observable ob, Object arg) {
        if (ob instanceof Point) {
            Point point = (Point) ob;
            JsonObject json = Json.createObjectBuilder()
                    .add("type", "cov")
                    .add("handlerId", handlerId)
                    .add("pointId", point.getId())
                    .add("pointName", point.getName())
                    .add("pv", point.getPresentValue())
                    .build();

            try {
                broadcast(json.toString());
            } catch (MqttException ex) {
                Logger.getLogger(MqttConsoleHandler.class.getName())
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
            
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                address = addresses.nextElement();
                if(address instanceof Inet4Address){
                    ipAddress = address.getHostAddress();
                    break;
                }
            }
        }

        return ipAddress;
    }
}
