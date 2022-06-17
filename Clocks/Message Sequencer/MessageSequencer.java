import java.util.ArrayList;

class MessageSequencer extends Thread{
    protected ArrayList<Message> queue;
    private MessageGenerator[] generators;
    private volatile boolean running = true;
    private int msgCount = 0;

    public MessageSequencer(MessageGenerator[] generators) {
        this.queue = new ArrayList<>();
        this.generators = generators;
    }

    // terminate thread
    public void terminate() {
        this.running = false;
    }

    public void receiveMsg(Message msg) {
        this.queue.add(msg);
    }

    // check new message
    private boolean checkForMessage() {
        return this.msgCount < this.queue.size();
    }

    // broadcast internal messages
    private void handleMsg() {
        Message msg = new Message(this.queue.get(this.msgCount));
        msg.setInternal(true);
        for (int i = 0; i < this.generators.length; i++) {
            synchronized (this.generators[i]) {
                this.generators[i].receiveMsg(msg);
                this.generators[i].notify();
            }
        }
        this.msgCount++;
    }

    @Override
    public void  run() {
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
