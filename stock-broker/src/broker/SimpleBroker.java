package src.broker;

import src.common.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.lang.reflect.Array;
import java.util.*;
import javax.jms.Queue;


public class SimpleBroker {
    /* TODO: variables as needed */
    private Connection connection;
    private Session session;
    private HashMap<String, MessageProducer> producers = new HashMap<>();
    private HashMap<String, MessageConsumer> consumer = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> clients = new HashMap<>();
    private ArrayList<Stock> stocks = new ArrayList<>();
    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            if (msg instanceof ObjectMessage) {
                //TODO
            }
        }
    };

    public SimpleBroker(List<Stock> stockList) throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connectionFactory.setTrustAllPackages(true);
        // start connection
        this.connection = connectionFactory.createConnection();
        this.connection.start();
        // create session
        this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // create register queue
        Queue registerQueue = this.session.createQueue("register");
        // create consumer
        MessageConsumer consumer = this.session.createConsumer(registerQueue);
        for (Stock stock : stockList) {
            /* TODO: prepare stocks as topics */
            Topic topic = this.session.createTopic(stock.getName());
            // store producer in the Hashmap
            this.producers.put(stock.getName(), this.session.createProducer(topic));
        }
        // store stocks
        this.stocks.addAll(stockList);
        consumer.setMessageListener(this.listener);
    }

    public void stop() throws JMSException {
        this.session.close();
        this.connection.close();
        System.exit(0);
    }

    public synchronized int buy(String stockName, int amount) throws JMSException {
        //TODO
        return -1;
    }

    public synchronized int sell(String stockName, int amount) throws JMSException {
        //TODO
        return -1;
    }

    public synchronized List<Stock> getStockList() {
        List<Stock> stockList = new ArrayList<>();

        /* TODO: populate stockList */

        return stockList;
    }
}
