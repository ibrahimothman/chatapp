package com.ibra.chatappdemo.model;

public class Message {
    private String message;
    private String time;
    private String type;
    private String seen;
    private String from;

    public Message(String message, String time, String type, String seen, String from) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.seen = seen;
        this.from = from;
    }

    public Message() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
