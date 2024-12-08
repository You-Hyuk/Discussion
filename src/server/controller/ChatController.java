package server.controller;

import server.domain.Chat;
import server.domain.Room;
import server.domain.User;
import server.repository.ChatRepository;
import server.repository.RoomRepository;

import java.io.PrintWriter;
import java.util.*;

import static server.dto.SuccessResponse.RECEIVE_CHAT_SUCCESS;

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
    }


    // 채팅방 입장
    public ArrayList<Chat> findChatHistory(Room room){
        ArrayList<Chat> chats = chatRepository.readChatHistory(room);
        return chats;
    }

    //상태에 따른 구분 필요
    public Chat chat(Room room, User user, HashMap userMap, String status, String content){

        Chat chat = new Chat(user.getUserName(), content, status);

        // 메시지 저장
        chatRepository.saveChat(room, chat);

        String userName = user.getUserName();

        // 모든 사용자에게 메시지 전송
        receiveChat(room, userName, chat, userMap);

        return chat;
    }

    public void receiveChat(Room room, String userName, Chat chat, HashMap<String, List<PrintWriter>> userMap) {
        // 방에 있는 사용자 리스트 가져오기
        List<PrintWriter> userList = userMap.get(room.getRoomName());

        if (userList == null || userList.isEmpty()) {
            System.out.println("비어있음");
            return; // 방에 사용자가 없으면 메시지를 전송하지 않음
        }

        synchronized (userList) {
            for (PrintWriter pw : userList) {
                String response = "[Response] UserName : " + userName +
                        " Command : " + RECEIVE_CHAT_SUCCESS.name() +
                        " Body : " +
                        " TimeStamp : " + chat.getTimestamp() +
                        " UserName : " + chat.getUserName() +
                        " Content : " +  chat.getMessage() +
                        " Status : " + chat.getStatus() +
                        " LikeCount : " + chat.getLike() +
                        " ChatId : " +  chat.getId();
                pw.println(response);
                pw.flush();
            }
        }
        System.out.println("[System] " + "Command : " + RECEIVE_CHAT_SUCCESS.name() + " RoomName : " +  room.getRoomName());
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
