package jot.chapter4.mqtt.thing;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JmsConsole {

    public static final String TOPIC_PREFIX = "jot.chapter4.mqtt.thing.";
    public static final String TOPIC_COMMAND = TOPIC_PREFIX + "%s.%s.command";
    public static final String TOPIC_RESULT = TOPIC_PREFIX + "%s.result";
    public static final String TOPIC_BROADCAST = TOPIC_PREFIX + "%s.broadcast";

    private String url;
    private String clientId;
    private Connection connection;
    private Session session;
    private String commandTopic;
    private String resultTopic;
    private String broadcastTopic;
    private MessageProducer commandProducer;
    private MessageConsumer resultConsumer;
    private MessageConsumer broadcastConsumer;
    private BigDataHandler bigData;

    public JmsConsole(String clientId, String handlerId,
            BigDataHandler bigData) throws JMSException {
        this.clientId = clientId;
        this.bigData = bigData;
        url = System.getProperty("jms.server");
        commandTopic = String.format(TOPIC_COMMAND, clientId, handlerId);
        resultTopic = String.format(TOPIC_RESULT, clientId);
        broadcastTopic = String.format(TOPIC_BROADCAST, handlerId);

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic commandDestination = session.createTopic(commandTopic);
        Topic resultDestination = session.createTopic(resultTopic);
        Topic broadcastDestination = session.createTopic(broadcastTopic);
        commandProducer = session.createProducer(commandDestination);
        resultConsumer = session.createConsumer(resultDestination);
        resultConsumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message msg) {
                try {
                    System.out.println("Result: " + convertMessageToString(msg));
                    System.out.print("input command or 'q'(quit): ");
                } catch (JMSException ex) {
                    Logger.getLogger(JmsConsole.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });

        broadcastConsumer = session.createConsumer(broadcastDestination);
        broadcastConsumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message msg) {
                try {
                    StringReader in
                            = new StringReader(convertMessageToString(msg));
                    JsonObject json = Json.createReader(in).readObject();
                    String broadcastType = json.getString("type");
                    if (broadcastType.equals(ChangeOfValue.TYPE)) {
                        ChangeOfValue cov = new ChangeOfValue(json);
                        bigData.saveBigData(cov);
                    } else {
                        System.out.println("Received a breoadcast: " + msg);
                    }
                } catch (JMSException e) {
                    Logger.getLogger(JmsConsole.class.getName())
                            .log(Level.SEVERE, null, e);
                }
            }
        });
    }

    public void publish(String payload) throws JMSException {
        BytesMessage message = session.createBytesMessage();
        message.writeBytes(payload.getBytes());
        commandProducer.send(message);
    }

    public void close() throws JMSException {
        connection.stop();
        commandProducer.close();
        resultConsumer.close();
        broadcastConsumer.close();
        connection.close();
    }

    private String convertMessageToString(Message msg) throws JMSException {
        if (!(msg instanceof BytesMessage)) {
            throw new JMSException("Only ByteMessage.");
        }

        BytesMessage response = (BytesMessage) msg;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int count = 0;
        while ((count = response.readBytes(buffer)) != -1) {
            baos.write(buffer, 0, count);
        }

        return baos.toString();
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Input ID: ");
        String clientId = input.nextLine();
        System.out.print("Input the IP Address of thing: ");
        String handlerId = input.nextLine().replace('.', '_');
        System.out.println("JmsConsole connecting...");
        try {
            BigDataHandler bigData = new BigDataHandler();
            JmsConsole console = new JmsConsole(clientId, handlerId, bigData);
            System.out.print("input command or 'q'(quit): ");
            for (String line = input.nextLine();
                    !line.equals("q");
                    line = input.nextLine()) {

                if (line.trim().length() == 0) {
                    continue;
                }

                switch (line) {
                    case "display":
                        bigData.displayBigData();
                        System.out.print("input command or 'q'(quit): ");
                        break;
                    case "clear":
                        bigData.clearBigData();
                        System.out.print("input command or 'q'(quit): ");
                        break;
                    default:
                        console.publish(line);
                        System.out.print("Waiting result....");
                }
            }
            console.close();
        } catch (JMSException ex) {
            Logger.getLogger(MqttConsole.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
