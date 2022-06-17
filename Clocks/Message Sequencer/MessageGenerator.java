import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MessageGenerator extends Thread {
    private final ArrayList<Message> queue; // incoming messages
    private volatile boolean running = true;
    private int msgCount = 0; // number of processed msg in queue
    private final MessageSequencer sequencer;

    public int id; //ThreadID

    // constructor for message sequencer
    public MessageGenerator(int id, MessageSequencer sequencer) {
        this.id = id;
        this.sequencer = sequencer;
        this.queue = new ArrayList<>();
    }

    // terminate thread
    public void terminate() {
        this.running = false;
        this.writeToFile();
    }

    // check new message
    private boolean checkForMessage() {
        return this.msgCount < this.queue.size();
    }

    public synchronized void receiveMsg(Message msg) {
        this.queue.add(msg);
    }

    // write messages from queue to log file
    private void writeToFile(){
        System.out.println("Write log Thread " + this.id);
        Path path = Paths.get("log_sequencer");
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
            for (Message message : this.queue){
                if (message.isInternal()){
                    str.append(message.getContent() + "\n");
                }
            }
        }

        String file = path + "/" + this.id + ".txt";
        try {
            Files.writeString(Paths.get(file), str, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // broadcast external / internal messages
    private void handleMsg() {
        Message msg = new Message(this.queue.get(this.msgCount)); // internal

        if (!msg.isInternal()){ // if external, forward to internal
            msg.setInternal(true);
            msg.setThreadId(this.id);
            synchronized (this.sequencer) {
                this.sequencer.receiveMsg(msg);
                this.sequencer.notify();
            }
        }
        this.msgCount++;
    }

    @Override
    public void run() {
        while(this.running) {
            try{
                synchronized (this) {
                    this.wait(); // wait for notification from sender
                }
            } catch(InterruptedException ex){
                break;
            }
            while(this.checkForMessage()) {
                this.handleMsg();
            }
        }
    }
}