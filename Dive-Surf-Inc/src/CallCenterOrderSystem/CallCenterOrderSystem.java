/*CallCenterOrderSystem: the Call center order system generates a text
file containing new orders every 2 minutes and stores it at a
predefined destination in the local file system. Each line represents a
single order. An order consists of comma-separated entries formatted
as <Customer-ID, Full Name, Number of ordered surfboards, Number
of ordered diving suits> - e.g., "1, Alice Test, 0, 1". The full name
always consists of the first and the last name separated by a space. It
is not defined how many orders are contained in the file.
 */
package CallCenterOrderSystem;
import Order.Order;
public class CallCenterOrderSystem {
    public static void main(String[] args) throws InterruptedException {
        boolean isTrue = true;
        Order order = new Order();
        int num = 0;

        while(isTrue){
            order.generate_file(num++);
            Thread.sleep(120000);
        }
    }
}
