package client.handler;


import java.io.PrintWriter;

import static client.dto.RequestCommand.GET_CHAT_HISTORY;
import static client.dto.RequestCommand.SEND_CHAT;

public class ChatHandler implements RequestBuilder {
    private PrintWriter pw;

    public ChatHandler(PrintWriter pw) {
        this.pw = pw;
    }

    public void getChaHistory(String roomName){
        String request = buildRequest(GET_CHAT_HISTORY.name(), roomName);
        pw.println(request);
        pw.flush();
    }

    public void sendChat(String roomName, String chat, String status){
        String request = buildRequest(SEND_CHAT.name(), roomName, chat, status);
        pw.println(request);
        pw.flush();
    }


    @Override
    public String buildRequest(String... params) {
        return String.join(" ", params);
    }
}
