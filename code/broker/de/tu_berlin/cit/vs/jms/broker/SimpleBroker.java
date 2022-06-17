package de.tu_berlin.cit.vs.jms.broker;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;

import de.tu_berlin.cit.vs.jms.common.BrokerMessage;
import de.tu_berlin.cit.vs.jms.common.RegisterMessage;
import de.tu_berlin.cit.vs.jms.common.Stock;
import de.tu_berlin.cit.vs.jms.common.UnregisterMessage;
import org.apache.activemq.ActiveMQConnectionFactory;


public class SimpleBroker {
    private Connection connection;
    private Session session;
    private HashMap<String, MessageProducer> producers = new HashMap<>();
    private HashMap<String, MessageConsumer> consumers = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> clients = new HashMap<>();
    private ArrayList<Stock> stocks = new ArrayList<>();
    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            if(msg instanceof ObjectMessage) {
                //TODO
                String clients;
                int amount;

            }
        }
    };
    
    public SimpleBroker(List<Stock> stockList) throws JMSException {
        /* initialize connection, sessions, etc. */
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connectionFactory.setTrustAllPackages(true);
        //start connection
        this.connection = connectionFactory.createConnection();
        this.connection.start();
        //create session
        this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //create register queue
        Queue reg_queue = this.session.createQueue("register");
        //create consumer
        MessageConsumer consumer = this.session.createConsumer(reg_queue);
        
        for(Stock stock : stockList) {
            //prepare stocks as topics
            Topic topic = this.session.createTopic(stock.getName());
            // store producer to Hashmap
            this.producers.put(stock.getName(), this.session.createProducer(topic));
        }
        //store stocks
        this.stocks.addAll(stockList);
        consumer.setMessageListener(this.listener);
    }
    
    public void stop() throws JMSException {
        this.session.close();
        this.connection.close();
        System.exit(0);
    }
    
    public synchronized int buy(String clientName, String stockName, int amount) throws JMSException {
        Stock stockToBuy = null;
        for (Stock stock : this.stocks) {
            if (stock.getName().equals(stockName)) {
                stockToBuy = stock;
            }
        }

        // remove stock to protect from concurrent write attempt
        this.stocks.remove(stockToBuy);
        if (stockToBuy == null) {
            return -1;
        }

        if (stockToBuy.getAvailableCount() <= 0 || stockToBuy.getAvailableCount() - amount < 0) {
            this.stocks.add(stockToBuy);
            return -1;
        }

        if (this.clients.containsKey(clientName)) {
            HashMap<String, Integer> ownedStocks = this.clients.get(clientName);
            if (ownedStocks.containsKey(stockName)) {
                // broker knows the client, they already own the stock
                int owned = ownedStocks.get(stockName);
                ownedStocks.remove(stockName);
                owned = owned + amount;
                ownedStocks.put(stockName, owned);
            } else {
                // client doesn't own this stock yet
                ownedStocks.put(stockName, amount);
            }
            this.clients.replace(clientName, ownedStocks);
        } else {
            // client buying for the first time
            HashMap<String, Integer> ownedStocks = new HashMap<>();
            ownedStocks.put(stockName, amount);
            this.clients.put(clientName, ownedStocks);
        }
        stockToBuy.setAvailableCount(stockToBuy.getAvailableCount() - amount);
        this.stocks.add(stockToBuy);
        return 1;
    }
    
    public synchronized int sell(String clientName, String stockName, int amount) throws JMSException {
        //TODO
        Stock stockToSell = null;
        for (Stock stock : this.stocks){
            if (stock.getName().equals(stockName)){
                stockToSell = stock;
            }
        }

        //remove stock to protect from concurrent write attempt
        this.stocks.remove(stockToSell);
        if (stockToSell == null)
            return -1;
        if (amount < 0){
            this.stocks.add(stockToSell);
            return -1;
        }
        if(this.clients.containsKey(clientName)){
            // broker knows the client already
            HashMap<String, Integer> ownedStocks = this.clients.get(clientName);
            System.out.println(ownedStocks);
            if(ownedStocks.containsKey(stockName)){
                //client owns some of the stock already
                int owned = ownedStocks.get(stockName);
                if (owned >= amount){
                    // amount to be sold is smaller than client owns
                    ownedStocks.remove(stockName);
                    ownedStocks.put(stockName, owned - amount);
                    this.clients.replace(clientName, ownedStocks);
                }else{
                    //client try to sell more than they own
                    this.stocks.add(stockToSell);
                    return -1;
                }
            }else{
                // client doenst own this stock yet
                this.stocks.add(stockToSell);
                return -1;
            }
        }else{
            //client try to sell the stock they don't own
            this.stocks.add(stockToSell);
            return -1;
        }
        stockToSell.setAvailableCount(stockToSell.getAvailableCount() + amount);
        this.stocks.add(stockToSell);
        return 1;
    }
    
    public synchronized List<Stock> getStockList() {
        List<Stock> stockList = new ArrayList<>();

        /* TODO: populate stockList */
        stockList.addAll(this.stocks);
        return stockList;
    }
}
