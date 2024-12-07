package client.handler;


import java.io.PrintWriter;

import static client.dto.RequestCommand.*;

public class ChatHandler implements RequestBuilder {
    private PrintWriter pw;
    private String userName;

    public ChatHandler(PrintWriter pw, String userName) {
        this.pw = pw;
        this.userName = userName;
    }

    public void getChaHistory(String roomName){
        String body = "RoomName :" + roomName;
        String request = buildRequest(userName, GET_CHAT_HISTORY.name(), body);
        pw.println(request);
        pw.flush();
    }

    public void sendChat(String roomName, String content, String status){
        String body = "RoomName :" + roomName + " Content :" + content + " Status :" + status;
        String request = buildRequest(userName, SEND_CHAT.name(), body);
        pw.println(request);
        pw.flush();
    }

    public void likeChat(String roomName, String chatId){
        String body = "RoomName :" + roomName + " ChatId :" + chatId;
        String request = buildRequest(userName, LIKE_CHAT.name(), body);
        pw.println(request);
        pw.flush();
    }


    @Override
    public String buildRequest(String userName, String command, String body) {
        return  "[REQUEST]" + "UserName :" + userName + " Command :" + command + " Body :" + body;
    }
}
