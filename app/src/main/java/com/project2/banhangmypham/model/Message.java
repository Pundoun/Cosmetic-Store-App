package com.project2.banhangmypham.model;
public class Message {
    String idMessage ;
    String idSender;
    String message ;
    Long timeSent ;
    String userUrlImage ;
    int status ; // 0 da gui, 1 da xem

    public String getIdMessage() {
        return idMessage;
    }

    public String getUserUrlImage() {
        return userUrlImage;
    }

    public void setUserUrlImage(String userUrlImage) {
        this.userUrlImage = userUrlImage;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Long timeSent) {
        this.timeSent = timeSent;
    }


    @Override
    public String toString() {
        return "Message{" +
                "idMessage='" + idMessage + '\'' +
                ", idSender='" + idSender + '\'' +
                ", message='" + message + '\'' +
                ", timeSent=" + timeSent +
                ", status=" + status +
                '}';
    }
}
