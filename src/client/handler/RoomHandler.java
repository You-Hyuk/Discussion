package client.handler;

import java.io.PrintWriter;

import static client.dto.RequestCommand.*;

public class RoomHandler implements RequestBuilder {
    private PrintWriter pw;
    private String userName;

    public RoomHandler(PrintWriter pw, String userName) {
        this.pw = pw;
        this.userName = userName;
    }

    public void getRoomList(){
        String body = " ";
        String request = buildRequest(userName, GET_ROOM_LIST.name(), body);
        pw.println(request);
        pw.flush();
    }

    public void findRoom(String roomName){
        String body = "RoomName : " + roomName;
        String request = buildRequest(userName, FIND_ROOM.name(), body);
        pw.println(request);
        pw.flush();
    }

    public void createRoom(String roomName, String firstStatus, String secondStatus){
        String body = "RoomName : " + roomName + " FirstStatus : " + firstStatus + " SecondStatus : " + secondStatus;
        String request = buildRequest(userName, CREATE_ROOM.name(), body);
        pw.println(request);
        pw.flush();
    }

    public void enterRoom(String roomName, String status){
        String body = "RoomName : " + roomName + " Status : " + status;
        String request = buildRequest(userName, ENTER_ROOM.name(), body);
        pw.println(request);
        pw.flush();
    }

    public void exitRoom(String roomName){
        String body = "RoomName : " + roomName;
        String request = buildRequest(userName, EXIT_ROOM.name(), body);
        pw.println(request);
        pw.flush();
    }

    public void voteDiscussion(String roomName, String status){
        String body = "RoomName : " + roomName + " Status : " + status;
        String request = buildRequest(userName, VOTE_DISCUSSION.name(), body);
        pw.println(request);
        pw.flush();
    }

    @Override
    public String buildRequest(String userName, String command, String body) {
        return  "[REQUEST] " + "UserName : " + userName + " Command : " + command + " Body : " + body;
    }
}
