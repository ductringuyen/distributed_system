import java.util.Random;

public class Aufgabe1 {
    public static void messageSequencer(int sumThreads, int sumMsg) throws InterruptedException {
        System.out.println("Start message sequencer threads.");
        MessageGenerator[] threads = new MessageGenerator[sumThreads];  // create threads array
        MessageSequencer seq = new MessageSequencer(threads);           // create sequencer

        // create & start threads
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MessageGenerator(i, seq);
            threads[i].start();
        }
        
        seq.start();

        int counter = 0;
        // send external messages to a thread
        while (counter < sumMsg) {
            int randomThread = new Random().nextInt(sumThreads);        // choose random thread
            int content = (int) (Math.random() * Integer.MAX_VALUE);    // generate random number
            Message msg = new Message(content, false);         // wrap random number as external msg
            threads[randomThread].receiveMsg(msg);                      // a thread will receive this msg
            synchronized (threads[randomThread]) {                      // wake that one thread
                threads[randomThread].notify();
            }
            counter++;
        }
        // sleep before terminate
        Thread.sleep(2 * sumMsg);

        // kill threads
        for (MessageGenerator thread : threads) {
            thread.terminate();
            thread.interrupt();
        }

        seq.terminate();
        seq.interrupt();
    }

    public static void main(String[] args) throws InterruptedException {
        int sumThreads = 0;
        int sumMsg = 0;
        if (args.length == 2) {
            try {
                sumThreads = Integer.parseInt(args[0]);
                sumMsg = Integer.parseInt(args[1]);
            } 
            catch (NumberFormatException e) {
                System.err.println("Only accept integer as argument.");
                System.exit(1);
            }
            messageSequencer(sumThreads, sumMsg);
            System.exit(0);
        } 
        else {
            System.out.println("Please specify number of threads and messages");
            System.exit(1);
        }
    }
    
}