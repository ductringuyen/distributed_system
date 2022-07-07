/*
The BillingSystem takes transformed order messages and
tests whether the customer is in good credit standing and is
allowed to order the requested items. Therefore, the billing system
modifies the valid property of the incoming messages and optionally
modifies the validationResult property.
 */
package BillingSystem;

import Order.Order;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.Random;

public class BillingSystem {
    private static final Random rand = new Random();

    private static final Processor BillCheck = exchange -> {
        Order order = (Order) exchange.getIn().getBody();

        boolean isValid =rand.nextBoolean();
        order.setValid(Boolean.toString(isValid));
        order.setValid(Boolean.toString(isValid));
        System.out.println("Order: " +order.getOrderID()+ " is " +isValid);
        //return Exchange
        exchange.getIn().setBody(order);
    };

    public static void main(String[] args) throws Exception {
        DefaultCamelContext defaultcamelcontext = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        defaultcamelcontext.addComponent("activeMQ", activeMQComponent);

        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() {
                from("activeMQ:topic:Orders")
                        .process(BillCheck)
                        .to("activeMQ:queue:validated");
            }
        };
        defaultcamelcontext.addRoutes(route);
        defaultcamelcontext.start();
        System.in.read();
        defaultcamelcontext.stop();
    }
}
