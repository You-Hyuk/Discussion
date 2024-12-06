package server.application;

import server.controller.ChatController;
import server.controller.RoomController;
import server.repository.ChatRepository;
import server.repository.RoomRepository;
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

            //클라이언트가 접속 되면 id를 전송한다는 프로토콜을 정의했기 때문에 readLine()을 통해 id를 받는다
            userName = br.readLine();
            user = new User(userName, pw);
            System.out.println("userName = " + userName);

            //chatController.printRoomList(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ", 2); // 최대 두 부분으로 나누기

                String command = parts[0]; // "CREATE_ROOM"
                String body = parts.length > 1 ? parts[1] : "";

                System.out.println("Command : " + command);
                System.out.println("Contents : " + body);

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
                        pw.println(chat.getTimestamp() + " " + chat.getUserName() + ": " + chat.getMessage());
                        pw.flush();
                    }
                    pw.println(GET_CHAT_HISTORY_SUCCESS.name()); // 종료 신호
                    pw.flush();
                }

                if (command.equals(EXIT_ROOM.name())){
                    String roomName = body.split(" ")[0];
                    Room room = roomController.findRoomByName(roomName);
                    chatController.exitRoom(room, user);
                    roomController.removeUserFromRoom(room.getRoomName(), user.getPrintWriter());
                    pw.println("EXIT_END"); // 종료 신호 전송
                    pw.flush();
                }


//                if(line.split(" ")[0].equals("/exit") && inRoom) {
//                    chatController.exitRoom(room, user);
//                    roomController.removeUserFromRoom(room.getRoomName(), user.getPrintWriter());
//                    pw.println("EXIT_END"); // 종료 신호 전송
//                    pw.flush();
//                    inRoom = false;
//                }
//
//                if (line.startsWith("/chat")) {
//                    if (line.length() <= 6) {
//                        pw.println("Error: 메시지가 비어 있습니다.");
//                        pw.flush();
//                        continue;
//                    }
//                    String content = line.substring(6).trim();
//                    System.out.println("content 확인: " + content);
//
//                    int lastIndex = content.lastIndexOf(" ");
//                    if (lastIndex == -1) {
//                        pw.println("Error: 메시지 형식이 잘못되었습니다.");
//                        pw.flush();
//                        continue;
//                    }
//
//                    String message = content.substring(0, lastIndex).trim();
//                    String status = content.substring(lastIndex + 1).trim();
//
//                    if (status == null || status.isEmpty()) {
//                        status = "중립";
//                    }
//
//                    Chat chat = new Chat(user.getUserName(), message, status);
//                    chatRepository.saveChat(room, chat);
//                    List<PrintWriter> userWriters = userMap.get(room.getRoomName());
//                    if (userWriters != null) {
//                        for (PrintWriter writer : userWriters) {
//                            writer.println("CHAT:" + chat.getTimestamp() + " " + chat.getUserName() + ": " + chat.getMessage());
//                            writer.flush();
//                        }
//                    }
//                    // 클라이언트로 확인 메시지 전송
//                    pw.println("CHAT_SAVED");
//                    pw.flush();
//                    pw.println("CHAT_END");
//                    pw.flush();
//                }

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