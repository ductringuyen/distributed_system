/* The ResultSystem collects the processed orders and prints a message
to the console once a new order is received. It must distinguish
between valid and invalid orders.
 */
package ResultSystem;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import Order.Order;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.io.IOException;

public class ResultSystem {
    public static class BooleanAggregation implements AggregationStrategy{
        @Override
        public Exchange aggregate(Exchange exchange1, Exchange exchange2){
            Order order1 = (Order) exchange1.getIn().getBody();
            Order order2 = (Order) exchange2.getIn().getBody();

            Boolean isValid1 = Boolean.parseBoolean(order1.getValid());
            Boolean isValid2 = Boolean.parseBoolean(order2.getValid());

            order2.setValidationResult(Boolean.toString(isValid1 && isValid2));

            exchange2.getIn().setBody(order2);

            return exchange2;

        }
    }

    public static void main(String[] args) throws Exception {
        DefaultCamelContext defaultCamelContext = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        defaultCamelContext.addComponent("activemq", activeMQComponent);

        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:queue:validated").aggregate((Expression) constant(true), (org.apache.camel.processor.aggregate.AggregationStrategy) new BooleanAggregation())
                        .completionInterval(5).choice().when(header("validationResult")).multicast()
                        .to("activemq:topic:processed", "stream:out");
            }
        };
        defaultCamelContext.addRoutes(route);
        defaultCamelContext.start();
        System.in.read();
        defaultCamelContext.stop();
    }
}
