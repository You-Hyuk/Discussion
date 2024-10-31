package server.domain;

import java.io.Serializable;
import java.sql.Timestamp;

public class Chat implements Serializable {
    private String userName;
    private String message;
    private Status status;
    private Timestamp timestamp;
    private Integer like;

    public Chat(String userName, String message, Status status) {
        this.userName = userName;
        this.message = message;
        this.status = status;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.like = 0;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    @Override
    public String toString(){
        return this.userName + "\t" + this.message + "\t" + this.status + "\t" + this.timestamp + "\t" + this.like;
    }

}
