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
    private RoomRepository roomRepository = new RoomRepository();
    private ChatRepository chatRepository = new ChatRepository();


    public void createRoom(Room room){
        chatRepository.createChatFile(room);
        roomRepository.createRoom(room);
        System.out.println(room.getRoomName() + " 생성 완료");
    }

    public void printRoomList(User user){
        ArrayList<Room> rooms = roomRepository.getRoomList();
        synchronized (user) {
            PrintWriter pw = user.getPrintWriter();
            pw.println("------------- 토론 채팅방 리스트 -------------");
            pw.flush();
            for (Room room : rooms) {
                pw.println(room.toString());
            }
            pw.println("----------------------------------------------");
            pw.flush();
        }
    }


    // 채팅방 입장
    public Room enterRoom(String roomName, User user){
        Room room = roomRepository.addUserToRoom(roomName, user);
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

    // 채팅방 Status 선택
    public void selectStatus(String roomName, User user){
        Room room = roomRepository.findRoomByName(roomName);
        String firstStatus = room.getFirstStatus();
        String secondStatus = room.getSecondStatus();

        synchronized (user){
            PrintWriter printWriter = user.getPrintWriter();
            printWriter.println("Status 선택");
            printWriter.println(firstStatus + "\t\t\t" + "NONE" + "\t\t\t" + secondStatus);
            printWriter.flush();
        }
    }

    // 채팅방 퇴장
    public void exitRoom(Room room,User user){
        room.removeUser(user);
        System.out.println(user.getUserName() + " 님이 " + room.getRoomName() + " 에서 퇴장하였습니다.");
    }

    //상태에 따른 구분 필요
    public void chat(Room room, User user, Status status, String message){
        ArrayList<User> userList = room.getUserList();
        String selectStatus = "";
        if (status == Status.STATUS1){
            selectStatus = room.getFirstStatus();
        }
        if (status == Status.STATUS2){
            selectStatus = room.getSecondStatus();
        }

        String chat = selectStatus + " : " + message;
        synchronized (userList){
            Collection<PrintWriter> collection = new ArrayList<>();
            for (User user1 : userList) {
                collection.add(user1.getPrintWriter());
            }
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
                PrintWriter pw = (PrintWriter) iter.next();
                pw.println(chat);
                pw.flush();
            }
        }
        String userName = user.getUserName();
        Chat chatHistory = new Chat(userName, chat, status);
        chatRepository.saveChat(room, chatHistory);
    }

    public void sendToClient(User user, String msg){
        synchronized (user) {
            PrintWriter pw = user.getPrintWriter();
            pw.println(msg);
            pw.flush();
        }
    }

}
