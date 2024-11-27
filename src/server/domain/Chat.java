package server.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

public class Chat implements Serializable {
    private String userName;
    private String message;
    private String status;
    private Timestamp timestamp;
    private Integer like;
    private String id;

    public Chat(String userName, String message, String status) {
        this.userName = userName;
        this.message = message;
        this.status = status;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.like = 0;
        this.id = UUID.randomUUID().toString();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public void incrementLike() {
        this.like++;
    }//좋아요 증가

    public String getId() {
        return id;
    }
    @Override
    public String toString(){
        return this.userName + "\t" + this.message + "\t" + this.status + "\t" + this.timestamp + "\t" + this.like;
    }

}