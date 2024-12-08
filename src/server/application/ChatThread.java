package server.application;

import server.controller.ChatController;
import server.controller.RoomController;
import server.domain.Chat;
import server.domain.Room;
import server.domain.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static client.dto.RequestCommand.*;
import static server.dto.ErrorResponse.FIND_ROOM_FAILED;
import static server.dto.SuccessResponse.*;

public class ChatThread extends Thread {
    private Socket sock;
    private String userName;
    private BufferedReader br;  //buffer 사용 용도
    private PrintWriter pw;  //println() 사용 용도
    private User user;
    private String status;
    private HashMap<String, List<PrintWriter>> userMap;

    private ChatController chatController;
    private RoomController roomController;

    public ChatThread(Socket sock, HashMap userMap) {
        this.sock = sock;
        this.userMap = userMap;
        this.roomController = new RoomController(userMap);
        this.chatController = new ChatController(userMap);
        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            userName = br.readLine();
            user = new User(userName, pw);
            System.out.println("userName = " + userName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                HashMap<String, String> parseRequest = parseRequest(line);
                String userName = parseRequest.get("UserName");
                String command = parseRequest.get("Command");
                String body = parseRequest.get("Body");

                HashMap<String, String> parsedBody = parseBody(body);

                if (command.equals(GET_ROOM_LIST.name())){
                    getRoomList();
                }

                if (command.equals(CREATE_ROOM.name())){
                    createRoom(parsedBody, userName);
                }

                if (command.equals(FIND_ROOM.name())){
                    findRoom(parsedBody);
                }

                if (command.equals(ENTER_ROOM.name())){
                    enterRoom(parsedBody);
                }

                if (command.equals(GET_CHAT_HISTORY.name())){
                    getChatHistory(parsedBody);
                }

                if (command.equals(EXIT_ROOM.name())){
                    exitRoom(parsedBody);
                }

                if (command.equals(SEND_CHAT.name())){
                    sendChat(parsedBody);
                }

                if (command.equals(VOTE_DISCUSSION.name())){
                    voteDiscussion(parsedBody);
                }

                if (command.equals(LIKE_CHAT.name())) {
                    likeChat(parsedBody);
                }


            }
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            try {
                if (sock != null)
                    sock.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void likeChat(HashMap<String, String> parsedBody) {
        String roomName = parsedBody.get("RoomName");
        String chatId = parsedBody.get("ChatId");

        Room room = roomController.findRoomByName(roomName);
        Integer likeCount = chatController.likeChat(room, chatId);


        // 클라이언트에 성공 응답 전송
        pw.println(LIKE_CHAT_SUCCESS.name() + " " + likeCount);
        pw.flush();

        String body = "LikeCount : " + likeCount;
        String response = buildResponse(userName, LIKE_CHAT_SUCCESS.name(), body);
        System.out.println(response);
    }

    private void voteDiscussion(HashMap<String, String> parsedBody) {
        String roomName = parsedBody.get("RoomName");
        String status = parsedBody.get("Status");

        roomController.vote(roomName, status);
        pw.println(VOTE_DISCUSSION_SUCCESS.name());
        pw.flush();

        String body = " ";
        String response = buildResponse(userName, VOTE_DISCUSSION_SUCCESS.name(), body);
        System.out.println(response);
    }

    private void sendChat(HashMap<String, String> parsedBody) {
        String roomName = parsedBody.get("RoomName");
        String content = parsedBody.get("Content");
        String status = parsedBody.get("Status");

        Room room = roomController.findRoomByName(roomName);

        Chat chat = chatController.chat(room, user, userMap, status, content);

        String body = "ChatId : " + chat.getId() + " LikeCount : " + chat.getLike() + " Content : " + content;
        String response = buildResponse(userName, SEND_CHAT_SUCCESS.name(), body);
        System.out.println(response);
    }

    private void exitRoom(HashMap<String, String> parsedBody) {
        String roomName = parsedBody.get("RoomName");
        roomController.removeUserFromRoom(roomName, user);
        pw.println(EXIT_ROOM_SUCCESS.name()); // 종료 신호 전송
        pw.flush();
    }

    private void getChatHistory(HashMap<String, String> parsedBody) {
        String roomName = parsedBody.get("RoomName");
        Room room = roomController.findRoomByName(roomName);
        ArrayList<Chat> chatHistory = chatController.findChatHistory(room);

        for (Chat chat : chatHistory) {
            pw.println(chat.getTimestamp() + " " + chat.getUserName() + " " + chat.getMessage() + " " + chat.getStatus() + " " + chat.getLike() + " " + chat.getId());
            pw.flush();
        }

        pw.println(GET_CHAT_HISTORY_SUCCESS.name()); // 종료 신호
        pw.flush();

        String body = " ";
        String response = buildResponse(userName, GET_CHAT_HISTORY_SUCCESS.name(), body);
        System.out.println(response);
    }

    private void enterRoom(HashMap<String, String> parsedBody) {
        String roomName = parsedBody.get("RoomName");
        String status = parsedBody.get("Stauts");

        // 유저 상태 설정
        user.setStatus(status);

        // 방 정보 업데이트
        Room enteredRoom = roomController.enterRoom(roomName, user);
        ArrayList<Chat> chatHistory = chatController.findChatHistory(enteredRoom);
        for (Chat chat : chatHistory) {
            pw.println(chat.getTimestamp() + " " + chat.getUserName() + " " + chat.getMessage() + " " + chat.getStatus() + " " + chat.getLike() + " " + chat.getId());
            pw.flush();
        }
        pw.println(ENTER_ROOM_SUCCESS.name()); // 종료 신호
        pw.flush();

        String body = " ";
        String response = buildResponse(userName, ENTER_ROOM_SUCCESS.name(), body);
        System.out.println(response);
    }

    private void findRoom(HashMap<String, String> parsedBody) {
        String roomName = parsedBody.get("RoomName");
        Room room = roomController.findRoomByName(roomName);

        if (room != null) {
            sendRoomData(room);
        } else {
            pw.println(FIND_ROOM_FAILED.name()); // 방을 찾을 수 없음
            pw.flush();
        }
        pw.println(FIND_ROOM_SUCCESS.name()); // 응답 종료
        pw.flush();

        String body = " ";
        String response = buildResponse(userName, FIND_ROOM_SUCCESS.name(), body);
        System.out.println(response);
    }

    private void createRoom(HashMap<String, String> parsedBody, String userName) {
        String roomName = parsedBody.get("RoomName");
        String firstStatus = parsedBody.get("FirstStatus");
        String secondStatus = parsedBody.get("SecondStatus");

        Room room = new Room(roomName, firstStatus, secondStatus, userName);
        chatController.createRoom(room);

        String body = " ";
        String response = buildResponse(userName, CREATE_ROOM_SUCCESS.name(), body);
        System.out.println(response);
    }

    private void getRoomList() {
        List<Room> roomList = roomController.getRoomList();
        for (Room room : roomList) {
            sendRoomData(room);
        }
        pw.println(GET_ROOM_LIST_SUCCESS.name());
        pw.flush();

        String body = " ";
        String response = buildResponse(userName, GET_ROOM_LIST_SUCCESS.name(), body);
        System.out.println(response);
    }

    private void sendRoomData(Room room) {
        String roomData = String.join(",",
                room.getRoomName(),
                room.getUserName(),
                String.valueOf(room.getFirstStatusCount()),
                String.valueOf(room.getSecondStatusCount()),
                room.getFirstStatus(),
                room.getSecondStatus()
        );
        pw.println(roomData);
        pw.flush();
    }

    private HashMap<String, String> parseRequest(String request) {
        HashMap<String, String> result = new HashMap<>();

        // 정규 표현식: Command 뒤의 모든 내용을 Body로 처리
        Pattern pattern = Pattern.compile("(?:\\[REQUEST\\])?UserName\\s*:\\s*(\\S+)\\s*Command\\s*:\\s*(\\S+)\\s*Body\\s*:\\s*(.*)");
        Matcher matcher = pattern.matcher(request);

        if (matcher.find()) {
            // 매칭된 값을 HashMap에 저장
            result.put("UserName", matcher.group(1).trim());
            result.put("Command", matcher.group(2).trim());
            result.put("Body", matcher.group(3).trim());
        }

        return result;
    }


    private HashMap<String, String> parseBody(String body) {
        HashMap<String, String> parsedData = new HashMap<>();

        // 정규 표현식 수정
        Pattern pattern = Pattern.compile("(\\w+)\\s*:\\s*(.*?)(?=\\s*\\w+\\s*:\\s*|$)");
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            String key = matcher.group(1).trim();  // Key
            String value = matcher.group(2).trim(); // Value
            parsedData.put(key, value);
        }

        return parsedData;
    }

    public String buildResponse(String userName, String command, String body) {
        return  "[Response] " + "UserName : " + userName + " Command : " + command + " Body : " + body;
    }
}