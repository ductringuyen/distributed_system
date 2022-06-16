package src.broker;

import src.common.*;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;


public class SimpleBroker {
    /* TODO: variables as needed */
    ConnectionFactory connectionFactory;
    Connection con;
    Session session;
    Queue queue;
    Topic topic;
    MessageProducer queueProducer;
    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            if (msg instanceof ObjectMessage) {
                //TODO
            }
        }
    };

    public SimpleBroker(List<Stock> stockList) throws JMSException, NamingException {

        Context ctx = new InitialContext();

        connectionFactory = (ConnectionFactory) ctx.lookup("/jms/TopicConnectionFactory");
        con = connectionFactory.createConnection();
        con.start();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) session.createQueue("myQueue");
        topic = session.createTopic("Topic_A");
        queueProducer = session.createProducer((Destination) queue);
        for (Stock stock : stockList) {
            /* TODO: prepare stocks as topics */
        }
    }

    public void stop() throws JMSException {

        con.stop();
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
