public class Message{

    private boolean internal;
    private int content;
    private int threadId;

    public Message(Message msg){
        this.content = msg.content;
        this.internal = msg.internal;
        this.threadId = msg.threadId;
    }

    public Message(int content, boolean internal) {
        this.content = content;
        this.internal = internal;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public int getContent() {
        return this.content;
    }

    public boolean isInternal() {
        return this.internal;
    }

}