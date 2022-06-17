import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MessageGenerator extends Thread {
    private final ArrayList<Message> queue;
    private volatile boolean isRunning = true;
    private int numMsg = 0; // number of processed msg in queue
    private int lamportCounter = 0;

    protected MessageGenerator[] generators;    //list of other thread to use with lamport
    public int id;  //ThreadID

    /*
     * Constructor for Lamport
     */
    public MessageGenerator(int id){
        this.id = id;
        this.queue = new ArrayList<>();
    }

    public void setGenerators(MessageGenerator[] generators){
        this.generators = generators;
    }

    /*
     * Check if a new mess arrived in queue
     */
    private boolean checkForMsg(){
        return this.numMsg < this.queue.size();
    }

    public synchronized void receiveMsg(Message msg){
        this.queue.add(msg);
    }

    /*
     * Terminate thread
     */
    public void terminate(){
        this.isRunning = false;
        this.writeToFile();
    }

    private void writeToFile(){
        System.out.println("Write log Thread " + this.id);
        Path path = Paths.get("log_lamport");
        // create dir
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                System.out.println("Error creating directory" + e.getMessage());
            }
        }

        StringBuilder str = new StringBuilder();
        synchronized (this){
            PriorityQueue<Message> lamportQueue = new PriorityQueue<>();
            //sort message based on lamport counter
            for(Message msg : this.queue){
                if(msg.isInternal()){
                    lamportQueue.add(msg);
                }
            }

            Message msg;
            while( (msg = lamportQueue.poll()) != null){
                str.append(msg.getContent()).append("\n");
            }
        }
        String file = path + "/" + this.id + ".txt";
        try {
            Files.writeString(Paths.get(file), str.toString(), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * Handle message with Lamport clock
     */
    private void handleMsgLamport(){
        Message msg = this.queue.get(this.numMsg); // internal

        if(!msg.isInternal()){ // if external, forward to internal
            this.lamportCounter++;
            msg.setLamportCounter(this.lamportCounter);
            msg.setThreadId(this.id);

            Message msgForward = new Message(msg);
            msgForward.setInternal(true);
            for(int i=0; i < this.generators.length; i++){
                //synchronized access
                synchronized (this.generators[i]){
                    this.generators[i].receiveMsg(msgForward);
                    //notify other except self
                    if(this.id != i) this.generators[i].notify();
                }
            }
        } else {
            //thread store maximum lamport counter
            this.lamportCounter = Math.max(this.lamportCounter, msg.getLamportCounter()) + 1;
            msg.setLamportCounter(this.lamportCounter);
        }
        this.numMsg++;
    }

    @Override
    public void run(){
        while(this.isRunning){
            try{
                synchronized (this){
                    this.wait();
                }
            }catch(InterruptedException ex){
                break;
            }
            while(this.checkForMsg()){
                this.handleMsgLamport();
            }
        }
    }


}