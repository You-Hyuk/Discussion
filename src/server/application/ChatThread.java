package server.application;
//senttoclient삭제
import server.controller.ChatController;
import server.controller.RoomController;
import server.repository.ChatRepository;
import server.repository.RoomRepository;
import server.domain.Chat;
import server.domain.Room;
import server.domain.User;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class ChatThread extends Thread {
    private Socket sock;
    private String userName;
    private BufferedReader br;  //buffer 사용 용도
    private PrintWriter pw;  //println() 사용 용도
    private User user;
    private Room room;
    private String status;
    private HashMap<String, List<PrintWriter>> userMap;

    private ChatRepository chatRepository;
    private RoomRepository roomRepository;
    private ChatController chatController;
    private boolean inRoom = false;
    private boolean hasStatus = false;
    private RoomController roomController;

    public ChatThread(Socket sock, HashMap userMap) {
        this.sock = sock;
        this.userMap = userMap;
        this.chatRepository = new ChatRepository();
        this.roomRepository = new RoomRepository();
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
                if(line.split(" ")[0].equals("/c")){
                    String[] s = line.split(" ");
                    String roomName = s[1];
                    String firstStatus = s[2];
                    String secondStatus = s[3];

                    Room room = new Room(roomName, firstStatus, secondStatus, userName);
                    chatController.createRoom(room);
                    //chatController.sendToClient(user, user.getUserName() + " 님이 " + roomName + " 토론 채팅방을 생성하였습니다.");
                }

                if(line.split(" ")[0].equals("/exit") && inRoom) {
                    String[] s = line.split(" ");
                    String roomName = s[1];
                    String selectedStatus = s[2];// 선택된 상태

                    if ("중립".equals(user.getStatus())) {
                        user.setStatus(selectedStatus);
                        System.out.println("중립 상태로 입장 후 상태 업데이트: " + selectedStatus);

                        // 상태별 카운트 업데이트
                        room = roomRepository.addUserToRoom(roomName, user);
                        roomRepository.saveSingleRoom(room);
                    }

                    // 퇴장 처리

                    chatController.exitRoom(room, user);
                    roomController.removeUserFromRoom(room.getRoomName(), user.getPrintWriter());
                    inRoom = false;
                    pw.println("EXIT_END"); // 종료 신호 전송
                    pw.flush();
                }

                //Status 선택 메소드 추가 필요
                if(line.split(" ")[0].equals("/e")) {
                    System.out.println("입장 명령 받음");
                    String[] s = line.split(" ");
                    String roomName = s[1];
                    String selectedStatus = s[2];
                    System.out.println("입장 roomName 확인: " + roomName);
                    System.out.println("선택된 상태 확인: " + selectedStatus);

                    // 유저 상태 설정
                    user.setStatus(selectedStatus);

                    // 방 정보 업데이트
                    Room enteredRoom = chatController.enterRoom(roomName, user);
                    if (enteredRoom != null) {
                        room = roomRepository.addUserToRoom(roomName, user); // 상태별 카운트 업데이트
                        pw.println("ROOM:" + enteredRoom.getRoomName());
                        pw.flush();

                        // 채팅 기록 전송
                        List<Chat> chatHistory = chatRepository.readChatHistory(enteredRoom);
                        for (Chat chat : chatHistory) {
                            pw.println(chat.getTimestamp() + " " + chat.getUserName() + ": " + chat.getMessage());
                            pw.flush();
                        }
                        pw.println("ENTER_END"); // 종료 신호
                        pw.flush();
                        inRoom = true;
                        System.out.println("chatThread, 입장, userMap.toString(): " + userMap.toString());
                    } else {
                        pw.println("ERROR: 방을 찾을 수 없습니다.");
                        pw.flush();
                    }
                }

                if (line.contains("/list")) {
                    List<Room> roomList = roomController.getRoomList();
                    for (Room room : roomList) {
                        String roomData = String.join(",",
                                room.getRoomName(),
                                room.getUserName(),
                                String.valueOf(room.getFirstStatusCount()),
                                String.valueOf(room.getSecondStatusCount()),
                                room.getFirstStatus(),
                                room.getSecondStatus()
                        );
                        System.out.println("roomData확인: " + roomData);
                        pw.println(roomData);
                        pw.flush();
                    }
                    pw.println("LIST_END"); // 응답 종료
                    pw.flush();
                }

                if (line.startsWith("/chat")) {
                    if (line.length() <= 6) {
                        pw.println("Error: 메시지가 비어 있습니다.");
                        pw.flush();
                        continue;
                    }
                    String content = line.substring(6).trim();
                    System.out.println("content 확인: " + content);

                    int lastIndex = content.lastIndexOf(" ");
                    if (lastIndex == -1) {
                        pw.println("Error: 메시지 형식이 잘못되었습니다.");
                        pw.flush();
                        continue;
                    }

                    String message = content.substring(0, lastIndex).trim();
                    String status = content.substring(lastIndex + 1).trim();

                    if (status == null || status.isEmpty()) {
                        status = "중립";
                    }

                    Chat chat = new Chat(user.getUserName(), message, status);
                    chatRepository.saveChat(room, chat);
                    List<PrintWriter> userWriters = userMap.get(room.getRoomName());
                    if (userWriters != null) {
                        for (PrintWriter writer : userWriters) {
                            writer.println("CHAT:" + chat.getTimestamp() + " " + chat.getUserName() + ": " + chat.getMessage());
                            writer.flush();
                        }
                    }
                    // 클라이언트로 확인 메시지 전송
                    pw.println("CHAT_SAVED");
                    pw.flush();
                    pw.println("CHAT_END");
                    pw.flush();
                }


                if (line.startsWith("/find")) {
                    String[] commandParts = line.split(" ");
                    if (commandParts.length < 2) {
                        pw.println("ERROR");
                        pw.flush();
                        continue;
                    }
                    String roomName = commandParts[1];
                    System.out.println("commandParts[1]을 roomName으로 받는지 확인: " + roomName);
                    Room room = roomRepository.findRoomByName(roomName);
                    System.out.println("room 확인: " + room);
                    if (room != null) {
                        String roomData = String.join(",",
                                room.getRoomName(),
                                room.getFirstStatus(),
                                room.getSecondStatus(),
                                room.getUserName(),
                                String.valueOf(room.getFirstStatusCount()),
                                String.valueOf(room.getSecondStatusCount())
                        );
                        pw.println(roomData); // 방 정보 전송
                        pw.flush();
                    } else {
                        pw.println("ERROR"); // 방을 찾을 수 없음
                        pw.flush();
                    }
                    pw.println("FIND_END"); // 응답 종료
                    pw.flush();
                }

                if (line.startsWith("/history")) {
                    String[] commandParts = line.split(" ");
                    if (commandParts.length < 2) {
                        pw.println("ERROR: 방 이름을 입력하세요.");
                        pw.flush();
                        continue;
                    }
                    String roomName = commandParts[1];
                    System.out.println("/chatHistory 요청, 방 이름: " + roomName);

                    // Room 객체를 통해 채팅 기록 가져오기
                    Room room = roomRepository.findRoomByName(roomName);
                    System.out.println("/chatHistory에서 room 확인: " + room);
                    if (room == null) {
                        pw.println("ERROR: 해당 방을 찾을 수 없습니다.");
                        pw.flush();
                        continue;
                    }

                    List<Chat> chatHistory = chatRepository.readChatHistory(room);
                    System.out.println("chathistory확인: " + chatHistory);
                    if (chatHistory == null || chatHistory.isEmpty()) {
                        pw.println("해당 방의 채팅 기록이 없습니다.");
                        pw.flush();
                        continue;
                    }

                    for (Chat chat : chatHistory) {
                        String formattedChat = String.join("\t",
                                chat.getUserName(),
                                chat.getMessage(),
                                chat.getStatus(),
                                chat.getTimestamp().toString(),
                                chat.getLike().toString(),
                                chat.getId() // id 추가
                        );
                        pw.println(formattedChat);
                        pw.flush();
                    }
                    pw.println("HISTORY_END"); // 종료 신호
                    pw.flush();
                }

                if (line.startsWith("/like")) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length < 3) {
                        pw.println("ERROR: 올바르지 않은 좋아요 요청 형식입니다.");
                        pw.flush();
                        continue;
                    }
                    String roomName = parts[1];
                    String chatId = parts[2];

                    Room room = roomRepository.findRoomByName(roomName);
                    if (room == null) {
                        pw.println("ERROR: 방을 찾을 수 없습니다.");
                        pw.flush();
                        continue;
                    }

                    chatRepository.updateLikeCount(room,chatId); // 좋아요 수 증가

                    // 클라이언트에 성공 응답 전송
                    pw.println("LIKE_SUCCESS ");
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

    private User findUserByNickname(HashMap<String, List<PrintWriter>> userMap, String userName) {
        if (userMap == null || userMap.isEmpty()) {
            System.out.println("User map is empty or null.");
            return null;
        }

        for (String roomName : userMap.keySet()) {
            List<PrintWriter> writers = userMap.get(roomName);
            for (PrintWriter writer : writers){
                if (writer.equals(user.getPrintWriter()) && user.getUserName().equals(userName)) {
                    return user; // 찾은 User 객체 반환
                }
            }
        }

        System.out.println("User not found for nickname: " + userName);
        return null; // 해당 닉네임의 사용자를 찾을 수 없음
    }
}