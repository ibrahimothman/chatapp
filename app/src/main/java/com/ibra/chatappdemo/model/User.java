package com.ibra.chatappdemo.model;

public class User {

    private String uId;
    private String uName;
    private String uImage;
    private String uStatus;
    private String uPhone;
    private String uThumb;
    private String online;

    public User(String uId, String uName, String uImage, String uStatus, String uPhone, String thumb,String onlineIcon) {
        this.uId = uId;
        this.uName = uName;
        this.uImage = uImage;
        this.uStatus = uStatus;
        this.uPhone = uPhone;
        this.uThumb = thumb;
        this.online = onlineIcon;
    }


    public User() {
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getuThumb() {
        return uThumb;
    }

    public void setuThumb(String uThumb) {
        this.uThumb = uThumb;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuImage() {
        return uImage;
    }

    public void setuImage(String uImage) {
        this.uImage = uImage;
    }

    public String getuStatus() {
        return uStatus;
    }

    public void setuStatus(String uStatus) {
        this.uStatus = uStatus;
    }

    public String getuPhone() {
        return uPhone;
    }

    public void setuPhone(String uPhone) {
        this.uPhone = uPhone;
    }
}
