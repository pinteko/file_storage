package examples;

import java.util.Arrays;

public class Message {

    private String type;
    private int length;
    private byte[] data;

    public Message() {
    }

    public Message(String type, int length, byte[] data) {
        this.type = type;
        this.length = length;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", length=" + length +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
