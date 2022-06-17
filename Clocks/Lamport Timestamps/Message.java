
public class Message implements Comparable<Message>{

    private boolean internal;
    private int content;
    private int threadId;
    private int lamportCounter = -1;

    public Message(int content, boolean internal) {
        this.content = content;
        this.internal = internal;
    }

    public Message(int content, boolean internal, int lamportCounter) {
        this.content = content;
        this.internal = internal;
        this.lamportCounter = lamportCounter;
    }

    public Message(Message msg){
        this.content = msg.content;
        this.internal = msg.internal;
        this.threadId = msg.threadId;
        this.lamportCounter = msg.lamportCounter;
    }

    public int getThreadId() {
        return this.threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getLamportCounter() {
        return this.lamportCounter;
    }

    public void setLamportCounter(int lamportCounter) {
        this.lamportCounter = lamportCounter;
    }

    public int getContent() {
        return this.content;
    }

    public boolean isInternal() {
        return this.internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    // sort by lamport counter
    @Override
    public int compareTo(Message msg) {
        if (this == msg) return 0;
        if (this.getLamportCounter() == msg.getLamportCounter()) {
            return Integer.compare(this.getThreadId(), msg.getThreadId());
        } else {
            if (this.getLamportCounter() > msg.getLamportCounter()) return 1;
            else return -1;
        }
    }
}