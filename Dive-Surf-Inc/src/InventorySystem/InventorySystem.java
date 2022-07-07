/* The InventorySystem takes transformed order messages
and tests whether the requested items are available. Only one
inventory exists, which means that the InventorySystems checks both
types of items. Therefore, the inventory system modifies the valid
property of the incoming messages and optionally modifies the
validationResult property.
 */
package InventorySystem;

import Order.Order;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class InventorySystem {
    private static int totalSurfboards = 100;
    private static int totalDivingSuits = 100;

    public static void setTotalSurfboards(int total){
        totalSurfboards = total;
    }

    public static int getTotalSurfboards(){
        return totalSurfboards;
    }

    public static void setTotalDivingSuits(int total){
        totalDivingSuits = total;
    }

    public static int getTotalDivingSuits(){
        return totalDivingSuits;
    }

    public static boolean checkInvent(int sb, int ds){
        if(sb < 0 || ds < 0){
            return false;
        }

        boolean checkB = (getTotalSurfboards() - sb) > -1;
        boolean checkS = (getTotalDivingSuits() - ds) > -1;
        return (checkB && checkS);
    }

    public static void updateInvent(int sb, int ds){
        setTotalDivingSuits(getTotalDivingSuits() - ds);
        setTotalSurfboards(getTotalSurfboards() - sb);
    }

    private static Processor InventCheck = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            Order order = (Order) exchange.getIn().getBody();

            int sb = Integer.parseInt(order.getNumberOfSurfboards());
            int ds = Integer.parseInt(order.getNumberOfDivingSuits());

            order.setValid(Boolean.toString(checkInvent(sb,ds)));
            System.out.println("Order: " + order.getOrderID() + " is " + checkInvent(sb, ds));

            exchange.getIn().setBody(order);
        }
    };

    private static Processor InventUpdate = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            Order order = (Order) exchange.getIn().getBody();

            int sb = Integer.parseInt(order.getNumberOfSurfboards());
            int ds = Integer.parseInt(order.getNumberOfDivingSuits());

            updateInvent(sb,ds);
            order.setValid(Boolean.toString(checkInvent(sb,ds)));
            exchange.getIn().setBody(order);
        }
    };

    public static void main(String[] args) throws Exception {
        DefaultCamelContext defaultCamelContext = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        defaultCamelContext.addComponent("activemq", activeMQComponent);

        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:topic:Orders").process(InventCheck).to("activemq:queue:validated");
                from("activemq:topic:processed").process(InventUpdate).to("stream:out");
            }
        };

        defaultCamelContext.addRoutes(route);
        defaultCamelContext.start();
        System.in.read();
        defaultCamelContext.stop();


    }
}
