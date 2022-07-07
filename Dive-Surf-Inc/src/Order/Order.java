package Order;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

public class Order implements Serializable {
    private String CustomerID;
    private String FirstName;
    private String LastName;
    private String OverallItems;
    private String NumberOfDivingSuits;
    private String NumberOfSurfboards;
    private String OrderID;
    private String Valid;
    private String validationResult;

    public Order(){}
    public Order(String CustomerID, String FirstName, String LastName, String OverallItems, String NumberOfDivingSuits,
                 String NumberOfSurfboards, String OrderID, String Valid, String validationResult) {
        this.CustomerID = CustomerID;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.OverallItems = OverallItems;
        this.NumberOfDivingSuits = NumberOfDivingSuits;
        this.NumberOfSurfboards = NumberOfSurfboards;
        this.OrderID = OrderID;
        this.Valid = Valid;
        this.validationResult = validationResult;
    }

    public String getCustomerID(){
        return this.CustomerID;
    }

    public String getFirstName(){
        return this.FirstName;
    }

    public String getOverallItems(){
        return this.OverallItems;
    }

    public String getNumberOfDivingSuits(){
        return this.NumberOfDivingSuits;
    }

    public String getNumberOfSurfboards(){
        return this.NumberOfSurfboards;
    }

    public String getOrderID() {
        return this.OrderID;
    }

    public String getValid() {
        return this.Valid;
    }

    public String getValidationResult() {
        return this.validationResult;
    }

    public void setCustomerID(String customerID) {
        this.CustomerID = customerID;
    }

    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }

    public void setLastName(String lastName) {
        this.LastName = lastName;
    }

    public void setOverallItems(String overallItems) {
        this.OverallItems = overallItems;
    }

    public void setNumberOfDivingSuits(String numberOfDivingSuits) {
        this.NumberOfDivingSuits = numberOfDivingSuits;
    }

    public void setNumberOfSurfSuits(String numberOfSurfSuits) {
        this.NumberOfSurfboards = numberOfSurfSuits;
    }

    public void setOrderID(String orderID) {
        this.OrderID = orderID;
    }

    public void setValid(String valid) {
        this.Valid = valid;
    }

    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }

    public String generate_orders(int format){
        Random rand = new Random();
        String[] firstName = new String[]{ "Albert", "Alvin", "Alex", "Bill", "Carl", "Daimler", "Dexter", "Elvis",
            "Fried", "Felix", "Greg", "Hexa", "Holg", "Isa", "Jill", "Jarvix", "Joe", "Leant", "Montre", "Marry",
            "Mark", "Nash", "Otto", "Paul", "Peter", "Rosie", "Steve", "Tim", "Tina", "Victor", "Walter"};
        String[] lastName = new String[] { "Anton", "Anderson", "Acker", "Berghen", "Bongord", "Boyer", "Bojer",
            "Callos", "Cullier", "Deff", "Denian", "Ecker", "Freid", "Handz", "Haworth", "Heffner", "Hoffman",
            "Karros", "Knutzer", "Lievict", "Lawrence", "Mecky", "McConter", "Mill", "Myers", "Neindiesch",
            "Org", "Orweg", "Orie", "Paisier", "Parkteson", "Petty", "Quinn", "Quixie", "Rachelman", "Ruppnick",
            "Sage", "Schwanski", "Scheinz", "Schwarz", "Steven", "Schamalz", "Seindeberg", "Solzer" };
        String order_str = "";
        if (format == 1){
            order_str = firstName[rand.nextInt(firstName.length)] + ", " + lastName[rand.nextInt(lastName.length)] + ", "
                    + rand.nextInt(1000) + ", " + rand.nextInt(1000) + ", " + rand.nextInt(1000) + '\n';
        }else if (format == 2){
            order_str = rand.nextInt(1000) + ", " + firstName[rand.nextInt(firstName.length)] + ' '
                    + lastName[rand.nextInt(lastName.length)] + ", " + rand.nextInt(1000) + ", " + rand.nextInt(1000)
                    + '\n';
        }
        return order_str;
    }

    public void generate_file(int num){
        Random rand = new Random();
        File order = new File("/home/tringuyen/distributed_system/Dive-Surf-Inc/Order/order" + num);
        try {
            FileWriter writer = new FileWriter(order);
            for (int i=0; i <= rand.nextInt(10); i++)
                writer.write(generate_orders(2));
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
