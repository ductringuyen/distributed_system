package src.client;


import org.apache.activemq.memory.list.MessageList;
import src.common.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JmsBrokerClient {
    private final String clientName;
    private final Connection con;
    private final Session session;
    private final MessageProducer msg_producer;
    private final MessageConsumer msg_consumer;
    private final MessageProducer reg_producer;
    private HashMap<String, MessageConsumer> subscribers = new HashMap<>();
    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            if(msg instanceof TextMessage){
                String text;
                try{
                    text = ((TextMessage) msg).getText();
                    System.out.println(clientName + ": " + text);
                }catch (JMSException e){
                    e.printStackTrace();
                }
            } else if (msg instanceof ObjectMessage){
                ListMessage content = null;
                try{
                    content = (ListMessage) ((ObjectMessage) msg).getObject();
                }catch (JMSException e){
                    e.printStackTrace();
                }
                System.out.println(clientName + "\t List of the stocks: \n" + content.getStocks());
            }else{
                System.out.println(clientName + " Invalid message ");
            }
        }
    };
    public JmsBrokerClient(String clientName) throws JMSException {
        this.clientName = clientName;

        /* initialize connection, sessions, consumer, producer, etc. */
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        conFactory.setTrustAllPackages(true);
        con = conFactory.createConnection();
        con.start();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue prod_queue = session.createQueue("server_incoming"+this.clientName);
        msg_producer = session.createProducer(prod_queue);
        Queue cons_queue = session.createQueue("server_outgoing"+this.clientName);
        msg_consumer = session.createConsumer(cons_queue);
        // send register message
        Queue reg_queue = session.createQueue("register");
        reg_producer = session.createProducer(reg_queue);
        reg_producer.send(session.createObjectMessage(new RegisterMessage(clientName)));
        System.out.println("Client want to register themself as: " + clientName);
        msg_consumer.setMessageListener(this.listener);
    }

    public void requestList() throws JMSException {
        //TODO
    }

    public void buy(String stockName, int amount) throws JMSException {
        //TODO
    }

    public void sell(String stockName, int amount) throws JMSException {
        //TODO
    }

    public void watch(String stockName) throws JMSException {
        //TODO
    }

    public void unwatch(String stockName) throws JMSException {
        //TODO
    }

    public void quit() throws JMSException {
        con.stop();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the client name:");
            String clientName = reader.readLine();

            JmsBrokerClient client = new JmsBrokerClient(clientName);

            boolean running = true;
            while (running) {
                System.out.println("Enter command:");
                String[] task = reader.readLine().split(" ");

                synchronized (client) {
                    switch (task[0].toLowerCase()) {
                        case "quit":
                            client.quit();
                            System.out.println("Bye bye");
                            running = false;
                            break;
                        case "list":
                            client.requestList();
                            break;
                        case "buy":
                            if (task.length == 3) {
                                client.buy(task[1], Integer.parseInt(task[2]));
                            } else {
                                System.out.println("Correct usage: buy [stock] [amount]");
                            }
                            break;
                        case "sell":
                            if (task.length == 3) {
                                client.sell(task[1], Integer.parseInt(task[2]));
                            } else {
                                System.out.println("Correct usage: sell [stock] [amount]");
                            }
                            break;
                        case "watch":
                            if (task.length == 2) {
                                client.watch(task[1]);
                            } else {
                                System.out.println("Correct usage: watch [stock]");
                            }
                            break;
                        case "unwatch":
                            if (task.length == 2) {
                                client.unwatch(task[1]);
                            } else {
                                System.out.println("Correct usage: watch [stock]");
                            }
                            break;
                        default:
                            System.out.println("Unknown command. Try one of:");
                            System.out.println("quit, list, buy, sell, watch, unwatch");
                    }
                }
            }

        } catch (JMSException | IOException ex) {
            Logger.getLogger(JmsBrokerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
