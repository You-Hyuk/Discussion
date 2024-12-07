package server.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Chat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userName;
    private String message;
    private String status;
    private String timestamp;
    private Integer like;
    private String id;

    public Chat(String userName, String message, String status) {
        this.userName = userName;
        this.message = message;
        this.status = status;
        this.timestamp = formatTimestamp(System.currentTimeMillis());;
        this.like = 0;
        this.id = UUID.randomUUID().toString();
    }

    // Timestamp를 HH:mm 형식으로 변환
    private String formatTimestamp(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(millis));
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
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
    public void setId(String Id) {
        this.id=id;
    }

    @Override
    public String toString(){
        return this.userName + "\t" + this.message + "\t" + this.status + "\t" + this.timestamp + "\t" + this.like;
    }

}
