package com.ibra.chatappdemo.model;

public class Request {

    private String request_type;

    public Request(String request_type) {
        this.request_type = request_type;
    }

    public Request() {
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
