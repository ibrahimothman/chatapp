package com.ibra.chatappdemo.model;

public class Widget {
    private String message;
    private String namne;
    private String time;


    public Widget() {
    }

    public Widget(String message, String namne, String time) {
        this.message = message;
        this.namne = namne;
        this.time = time;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNamne() {
        return namne;
    }

    public void setNamne(String namne) {
        this.namne = namne;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
