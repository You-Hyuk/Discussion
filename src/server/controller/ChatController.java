package server.controller;

import server.domain.Chat;
import server.domain.Room;
import server.domain.Status;
import server.domain.User;
import server.repository.ChatRepository;
import server.repository.RoomRepository;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ChatController {
    private User user;
    private RoomRepository roomRepository = new RoomRepository();
    private ChatRepository chatRepository = new ChatRepository();


    public void createRoom(Room room){
        chatRepository.createChatFile(room);
        roomRepository.createRoom(room);
        System.out.println(room.getRoomName() + " 생성 완료");
    }


    // 방 입장
    public Room enterRoom(String roomName, User user){
        Room room = roomRepository.findRoomByName(roomName);
        room.addUser(user);
        System.out.println(user.getUserName() + " 님이 " + room.getRoomName() + " 에 입장하였습니다.");
        sendToClient(user,user.getUserName() + " 님이 " + roomName + " 토론 채팅방에 입장하였습니다.");
        ArrayList<Chat> chats = chatRepository.findChatHistory(room);
        synchronized (user) {
            PrintWriter pw = user.getPrintWriter();
            for (Chat chat : chats) {
                String message = chat.getMessage();
                pw.println(message);
                pw.flush();
            }
        }
        return room;
    }

    // 채팅방 퇴장
    public void exitRoom(Room room,User user){
        room.removeUser(user);
        System.out.println(user.getUserName() + " 님이 " + room.getRoomName() + " 방에서 퇴장하였습니다.");
    }

    //상태에 따른 구분 필요
    public void chat(Room room, User user, Status status, String message){
        ArrayList<User> userList = room.getUserList();
        synchronized (userList){
            Collection<PrintWriter> collection = new ArrayList<>();
            for (User user1 : userList) {
                collection.add(user1.getPrintWriter());
            }
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
                PrintWriter pw = (PrintWriter) iter.next();
                pw.println(message);
                pw.flush();
            }
        }
        String userName = user.getUserName();
        Chat chat = new Chat(userName, message, status);
        chatRepository.saveChat(room, chat);
    }

    public void sendToClient(User user, String msg){
        synchronized (user) {
            PrintWriter pw = user.getPrintWriter();
            pw.println(msg);
            pw.flush();
        }
    }

}
