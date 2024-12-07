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
                String[] parts = line.split(" ", 2); // 최대 두 부분으로 나누기

                String command = parts[0];
                String body = parts.length > 1 ? parts[1] : "";

                System.out.println("Command : " + command + ", Contents : " + body);

                if (command.equals(GET_ROOM_LIST.name())){
                    List<Room> roomList = roomController.getRoomList();
                    for (Room room : roomList) {
                        sendRoomData(room);
                    }
                    pw.println(GET_ROOM_LIST_SUCCESS.name());
                    pw.flush();
                }

                if (command.equals(CREATE_ROOM.name())){
                    String[] createRoomInfo = body.split(" ");
                    String roomName = createRoomInfo[0];
                    String firstStatus = createRoomInfo[1];
                    String secondStatus = createRoomInfo[2];

                    Room room = new Room(roomName, firstStatus, secondStatus, userName);
                    chatController.createRoom(room);
                }

                if (command.equals(FIND_ROOM.name())){
                    String roomName = body.split(" ")[0];
                    Room room = roomController.findRoomByName(roomName);

                    if (room != null) {
                        sendRoomData(room);
                    } else {
                        pw.println(FIND_ROOM_FAILED.name()); // 방을 찾을 수 없음
                        pw.flush();
                    }
                    pw.println(FIND_ROOM_SUCCESS.name()); // 응답 종료
                    pw.flush();
                }

                if (command.equals(ENTER_ROOM.name())){
                    String[] content = body.split(" ");
                    String roomName = content[0];
                    String selectedStatus = content[1];

                    // 유저 상태 설정
                    user.setStatus(selectedStatus);

                    // 방 정보 업데이트
                    Room enteredRoom = roomController.enterRoom(roomName, user);
                    ArrayList<Chat> chats = chatController.sendChatHistory(enteredRoom, user);
                    for (Chat chat : chats) {
                        pw.println(chat.getTimestamp() + " " + chat.getUserName() + ": " + chat.getMessage());
                        pw.flush();
                    }
                    pw.println(ENTER_ROOM_SUCCESS.name()); // 종료 신호
                    pw.flush();
                }

                if (command.equals(GET_CHAT_HISTORY.name())){
                    String roomName = body.split(" ")[0];
                    Room room = roomController.findRoomByName(roomName);
                    ArrayList<Chat> chatHistory = chatController.sendChatHistory(room, user);

                    for (Chat chat : chatHistory) {
                        pw.println(chat.getTimestamp() + " " + chat.getUserName() + " " + chat.getMessage() + " " + chat.getStatus() + " " + chat.getLike());
                        pw.flush();
                    }
                    pw.println(GET_CHAT_HISTORY_SUCCESS.name()); // 종료 신호
                    pw.flush();
                }

                if (command.equals(EXIT_ROOM.name())){
                    String roomName = body.split(" ")[0];
                    roomController.removeUserFromRoom(roomName, user);
                    pw.println(EXIT_ROOM_SUCCESS.name()); // 종료 신호 전송
                    pw.flush();
                }

                if (command.equals(SEND_CHAT.name())){
                    String roomName = body.split(" ")[0];
                    String content = body.split(" ")[1];
                    String status = body.split(" ")[2];

                    Room room = roomController.findRoomByName(roomName);

                    chatController.chat(room, user, userMap, status, content);

                    pw.println(SEND_CHAT_SUCCESS.name());
                    pw.flush();
                }

                if (command.equals(VOTE_DISCUSSION.name())){
                    String roomName = body.split(" ")[0];
                    String status = body.split(" ")[1];

                    roomController.vote(roomName, status);
                    pw.println(VOTE_DISCUSSION_SUCCESS.name());
                    pw.flush();
                }

                if (command.equals(LIKE_CHAT.name())) {
                    String roomName = parsedBody.get("RoomName");
                    String chatId = parsedBody.get("ChatId");

                    Room room = roomController.findRoomByName(roomName);
                    Integer likeCount = chatController.likeChat(room, chatId);

                    // 클라이언트에 성공 응답 전송
                    pw.println(LIKE_CHAT_SUCCESS.name() + " " + likeCount);
                    pw.flush();
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
}