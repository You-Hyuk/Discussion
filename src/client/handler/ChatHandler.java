package client.handler;


import java.io.PrintWriter;

import static client.dto.RequestCommand.GET_CHAT_HISTORY;
import static client.dto.RequestCommand.SEND_CHAT;

public class ChatHandler {
    private PrintWriter pw;

    public ChatHandler(PrintWriter pw) {
        this.pw = pw;
    }

    public void getChaHistory(String roomName){
        String request = GET_CHAT_HISTORY.name() + " " + roomName;
        pw.println(request);
        pw.flush();
    }

    public void sendChat(String chat){
        String request = SEND_CHAT.name() + " " + chat;
        pw.println(request);
        pw.flush();
    }

}
