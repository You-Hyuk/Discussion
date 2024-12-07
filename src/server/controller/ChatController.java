package server.controller;

import server.domain.Chat;
import server.domain.Room;
import server.domain.User;
import server.repository.ChatRepository;
import server.repository.RoomRepository;

import java.io.PrintWriter;
import java.util.*;

public class ChatController {
    private RoomRepository roomRepository = new RoomRepository();
    private ChatRepository chatRepository = new ChatRepository();
    private RoomController roomController;
    private Map<String, List<PrintWriter>> userMap;

    public ChatController(Map<String, List<PrintWriter>> userMap) {
        this.userMap = userMap;
    }

    public void createRoom(Room room){
        chatRepository.createChatFile(room);
        roomRepository.createRoom(room);
        System.out.println(room.getRoomName() + " 생성 완료");
    }


    // 채팅방 입장
    public ArrayList<Chat> sendChatHistory(Room room, User user){
        ArrayList<Chat> chats = chatRepository.readChatHistory(room);
        return chats;
    }

    //상태에 따른 구분 필요
    public Chat chat(Room room, User user, HashMap userMap, String status, String content){

        String userName = user.getUserName();
        Chat chat = new Chat(userName, content, status);

        List<PrintWriter> userList = (List<PrintWriter>) userMap.get(room.getRoomName());
        if (userList == null) {
            userList = new ArrayList<>(); // 빈 리스트로 초기화
            userMap.put(room.getRoomName(), userList); // userMap에 추가
        }

        if (userList != null) {
            synchronized (userList) {
                for (PrintWriter pw : userList) {
                    pw.println(chat.getTimestamp() + " " + chat.getUserName() + " " + chat.getMessage() + " " + chat.getStatus() + " " + chat.getLike()); // 메시지 출력
                    pw.flush();       // 버퍼 비우기
                }
            }
        }
        chatRepository.saveChat(room, chat);
        return chat;
    }

    public void deleteExpiredRooms() {
        RoomRepository roomRepository = new RoomRepository();
        roomRepository.deleteExpiredRooms(); // 만료된 방 삭제
    }

    public Integer likeChat(Room room, String chatId) {
        Integer likeCount = chatRepository.updateLikeCount(room, chatId);
        return likeCount;
    }

    // 좋아요 업데이트 브로드캐스트
    private void broadcastLikeUpdate(Room room, Chat chat) {
        List<PrintWriter> userList = userMap.get(room.getRoomName());
        if (userList == null) return;
        String likeUpdateMessage = "좋아요 업데이트: " +
                "Message ID: " + chat.getId() +
                " | Likes: " + chat.getLike();
        synchronized (userList) {
            for (PrintWriter pw : userList) {
                pw.println(likeUpdateMessage); // 좋아요 업데이트 메시지 전송
                pw.flush();
            }
        }
    }

    private void broadcastMostLikedChat(Room room) {
        ArrayList<Chat> chats = chatRepository.readChatHistory(room);
        Chat mostLikedChat = chatRepository.findMostLikedChat(chats);

        if (mostLikedChat != null) {
            List<PrintWriter> userList = userMap.get(room.getRoomName());
            if (userList == null) return;

            String popupMessage = "최고 좋아요 채팅: " + mostLikedChat.getMessage() +
                    " | Likes: " + mostLikedChat.getLike();

            synchronized (userList) {
                for (PrintWriter pw : userList) {
                    pw.println("POPUP: " + popupMessage); // 팝업 메시지 전송
                    pw.flush();
                }
            }
        }
    }


}
