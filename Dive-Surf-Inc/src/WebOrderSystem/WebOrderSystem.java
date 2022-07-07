/*WebOrderSystem: the web order system generates a string for each
new incoming order. The string needs to be processed further by the
integration solution. The string consists of comma-separated entries
and is formatted as follows: <First Name, Last Name, Number of
ordered surfboards, Number of ordered diving suits, Customer-ID> -
e.g., "Alice, Test, 2, 0, 1".
 */
package WebOrderSystem;

import org.apache.activemq.ActiveMQConnectionFactory;
import Order.Order;


import javax.jms.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class WebOrderSystem {
    private final Connection con;
    private final Session session;
    private final MessageProducer msg_producer;
    private static final String[] firstName = new String[]{ "Albert", "Alvin", "Alex", "Bill", "Carl", "Daimler", "Dexter", "Elvis",
            "Fried", "Felix", "Greg", "Hexa", "Holg", "Isa", "Jill", "Jarvix", "Joe", "Leant", "Montre", "Marry",
            "Mark", "Nash", "Otto", "Paul", "Peter", "Rosie", "Steve", "Tim", "Tina", "Victor", "Walter"};
    private static final String[] lastName = new String[] { "Anton", "Anderson", "Acker", "Berghen", "Bongord", "Boyer", "Bojer",
            "Callos", "Cullier", "Deff", "Denian", "Ecker", "Freid", "Handz", "Haworth", "Heffner", "Hoffman",
            "Karros", "Knutzer", "Lievict", "Lawrence", "Mecky", "McConter", "Mill", "Myers", "Neindiesch",
            "Org", "Orweg", "Orie", "Paisier", "Parkteson", "Petty", "Quinn", "Quixie", "Rachelman", "Ruppnick",
            "Sage", "Schwanski", "Scheinz", "Schwarz", "Steven", "Schamalz", "Seindeberg", "Solzer" };

    public WebOrderSystem() throws JMSException{
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connectionFactory.setTrustAllPackages(true);
        con = connectionFactory.createConnection();
        con.start();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic order_topic = session.createTopic("Orders");
        msg_producer = session.createProducer(order_topic);
    }

    public void stop() throws JMSException{
        msg_producer.close();
        session.close();
        con.close();
    }

    public static void main(String[] args) throws JMSException {
        WebOrderSystem webOrderSystem = new WebOrderSystem();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        int i =0;

        while (i<1){
            Order order = new Order(String.valueOf(i), firstName[i], lastName[i], String.valueOf(2*i), String.valueOf(i), String.valueOf(i), String.valueOf(i), "false", "false");
            webOrderSystem.msg_producer.send(webOrderSystem.session.createObjectMessage(order));
            i++;
        }

        webOrderSystem.stop();
    }
}
