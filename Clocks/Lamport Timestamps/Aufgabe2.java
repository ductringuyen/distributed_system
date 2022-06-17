import java.util.Random;

public class Aufgabe2 {
    public static void lamport(int sumThreads, int sumMsg) throws InterruptedException{
        System.out.println("Start lamport threads.");
        MessageGenerator[] threads = new MessageGenerator[sumThreads];

        // create & start threads
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MessageGenerator(i);
            threads[i].setGenerators(threads);
            threads[i].start();
        }

        int counter = 0;
        // send external messages
        while(counter < sumMsg){
            int randomThread = new Random().nextInt(sumThreads);
            int content = (int) (Math.random() * Integer.MAX_VALUE);
            Message msg = new Message(content, false, counter);
            threads[randomThread].receiveMsg(msg);
            synchronized (threads[randomThread]){
                threads[randomThread].notify();
            }
            counter++;
        }

        // sleep before terminate
        Thread.sleep(2 * sumMsg);

        // kill threads
        for(MessageGenerator thread : threads){
            thread.terminate();
            thread.interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int sumThreads = 0;
        int sumMsg =0;
        if(args.length == 2){
            try{
                sumThreads =Integer.parseInt(args[0]);
                sumMsg = Integer.parseInt(args[1]);
            } 
            catch(NumberFormatException e){
                System.err.println("Only accept integer as argument.");
                System.exit(1);
            }
            lamport(sumThreads, sumMsg);
            System.exit(0);
        }
        else {
            System.out.println("Please specify number of threads and messages");
            System.exit(1);
        }
    }
}