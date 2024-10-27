package server.domain;

import java.io.Serializable;
import java.sql.Timestamp;

public class Room implements Serializable {
    public String roomName;
    public String nickname;
    public String firstStatus;
    public String secondStatus;
    public Integer firstStatusCount;
    public Integer secondStatusCount;
    public Integer chatCount;
    public Timestamp timestamp;

    public Room(String roomName, String firstStatus, String secondStatus, String nickname) {
        this.roomName = roomName;
        this.nickname = nickname;
        this.firstStatus = firstStatus;
        this.secondStatus = secondStatus;
        this.firstStatusCount = 0;
        this.secondStatusCount = 0;
        this.chatCount = 0;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFirstStatus() {
        return firstStatus;
    }

    public void setFirstStatus(String firstStatus) {
        this.firstStatus = firstStatus;
    }

    public String getSecondStatus() {
        return secondStatus;
    }

    public void setSecondStatus(String secondStatus) {
        this.secondStatus = secondStatus;
    }

    public Integer getFirstStatusCount() {
        return firstStatusCount;
    }

    public void setFirstStatusCount(Integer firstStatusCount) {
        this.firstStatusCount = firstStatusCount;
    }

    public Integer getSecondStatusCount() {
        return secondStatusCount;
    }

    public void setSecondStatusCount(Integer secondStatusCount) {
        this.secondStatusCount = secondStatusCount;
    }

    public Integer getChatCount() {
        return chatCount;
    }

    public void setChatCount(Integer chatCount) {
        this.chatCount = chatCount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomName='" + roomName + '\'' +
                ", nickname='" + nickname + '\'' +
                ", firstStatus='" + firstStatus + '\'' +
                ", secondStatus='" + secondStatus + '\'' +
                ", firstStatusCount=" + firstStatusCount +
                ", secondStatusCount=" + secondStatusCount +
                ", chatCount=" + chatCount +
                ", timestamp=" + timestamp +
                '}';
    }
}
