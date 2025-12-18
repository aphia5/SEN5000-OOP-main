package common;

import java.io.Serializable;

public class Message implements Serializable {
    public enum Type {
        REQUEST,
        RESPONSE,
        ERROR,
        SUCCESS,
        MESSAGE,
        CLOSE
    }

    private Type type;
    private String content;

    public Message(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}