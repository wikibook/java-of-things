package jot.chapter4.mqtt;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JmsEchoSender {
    public static final String TOPIC_PREFIX = "jot.chapter4.mqtt.";
    public static final String TOPIC_ECHO = TOPIC_PREFIX + "%s.echo";
    public static final String TOPIC_RESPONSE = TOPIC_PREFIX + "%s.response";
    
    private String url;
    private String clientId;
    private Connection connection;
    private Session session;
    private String echoTopic;
    private String responseTopic;
    private MessageProducer echoProducer;
    private MessageConsumer responseConsumer;
    
    public JmsEchoSender(String clientId) throws JMSException{
        this.clientId = clientId;
        url = System.getProperty("jms.server");
        echoTopic = String.format(TOPIC_ECHO, clientId);
        responseTopic = String.format(TOPIC_RESPONSE, clientId);
        
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination echoDestination = session.createTopic(echoTopic);
        Destination responseDestination = session.createTopic(responseTopic);
        echoProducer = session.createProducer(echoDestination);
        responseConsumer = session.createConsumer(responseDestination);
        responseConsumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message msg) {
                try {
                    if(msg instanceof BytesMessage){
                        BytesMessage response = (BytesMessage) msg;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[512];
                        int count = 0;
                        while((count = response.readBytes(buffer)) != -1)
                            baos.write(buffer, 0, count);
                        
                        StringReader sr = new StringReader(baos.toString());
                        JsonObject json = Json.createReader(sr).readObject();
                        System.out.println("clientId: " + json.getString("clientId"));
                        System.out.println("message: " + json.getString("message"));
                        System.out.print("input message or 'q'(quit): ");
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(JmsEchoSender.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        });
    }
    
    public void publish(String payload) throws JMSException{
        BytesMessage message = session.createBytesMessage();
        message.writeBytes(payload.getBytes());
        echoProducer.send(message);
    }
    
    public void close() throws JMSException{
        connection.stop();
        echoProducer.close();
        responseConsumer.close();
        connection.close();
    }
    
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        // Scanner input = new Scanner(System.in, "euc-kr"); // 한글
        System.out.print("Input client id: ");
        String clientid = input.nextLine();
        System.out.print("MqttClient connecting...");
        try {
            JmsEchoSender client = new JmsEchoSender(clientid);
            System.out.print("input message or 'q'(quit): ");
            for (String line = input.nextLine(); 
                !line.equals("q"); 
                line = input.nextLine()) {
                client.publish(line);
                System.out.print("Waiting echo....");
            }
            client.close();
        } catch (JMSException ex) {
            Logger.getLogger(JmsEchoSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
