package server.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Room implements Serializable {
    public String roomName;
    public String userName;
    public String firstStatus;
    public String secondStatus;
    public Integer firstStatusCount;
    public Integer secondStatusCount;
    public Integer chatCount;
    public Timestamp timestamp;
    public ArrayList<User> userList;
    public String chatFileName;

    public Room(String roomName, String firstStatus, String secondStatus, String userName) {
        this.roomName = roomName;
        this.userName = userName;
        this.firstStatus = firstStatus;
        this.secondStatus = secondStatus;
        this.firstStatusCount = 0;
        this.secondStatusCount = 0;
        this.chatCount = 0;
        this.userList = new ArrayList<>();
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.chatFileName = "";
    }

    //Status를 입력하지 않았을 경우 생성자 필요

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
                ", nickname='" + userName + '\'' +
                ", firstStatus='" + firstStatus + '\'' +
                ", secondStatus='" + secondStatus + '\'' +
                ", firstStatusCount=" + firstStatusCount +
                ", secondStatusCount=" + secondStatusCount +
                ", chatCount=" + chatCount +
                ", timestamp=" + timestamp +
                '}';
    }

    // User 관리 메소드
    public void addUser(User user){
        userList.add(user);
    }

    public void removeUser(User user){
        userList.remove(user);
    }

    public ArrayList<User> getUserList(){
        return userList;
    }

    //채팅 기록 관리
    public void setChatFileName(String chatFileName){
        this.chatFileName = chatFileName;
    }

    public String getChatFileName() {
        return chatFileName;
    }
}
