package server.domain;

import java.sql.Timestamp;

public class Chat {
    public Integer id;
    public String nickname;
    public String contents;
    public Status status;
    public Timestamp timestamp;
    public Integer like;

    public Chat(Integer id, String nickname, String contents, Status status, Timestamp timestamp, Integer like) {
        this.id = id;
        this.nickname = nickname;
        this.contents = contents;
        this.status = status;
        this.timestamp = timestamp;
        this.like = like;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
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
        return this.id + "\t" + this.nickname + "\t" + this.contents+ "\t" + this.status + "\t" + this.timestamp + "\t" + this.like;
    }

}
