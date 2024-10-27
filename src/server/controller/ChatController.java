package server.controller;

import server.domain.Room;
import server.domain.User;
import server.repository.RoomRepository;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ChatController {
    private HashMap roomMap; //방 관리
    private HashMap userMap; //방 안에서의 Stream Map
    private User user;
    private RoomRepository roomRepository = new RoomRepository();

    public ChatController(HashMap roomMap, HashMap userMap) {
        this.roomMap = roomMap;
        this.userMap = userMap;
    }

    public void createRoom(Room room){
        HashMap um = new HashMap();
        synchronized (roomMap) {
            roomMap.put(room, um);
        }
        System.out.println(room.getRoomName() + " 생성 완료");
    }

    public void removeRoom(Room room){
        synchronized (roomMap){
            roomMap.remove(room);
        }
    }

    // 방 입장
    public void enterRoom(String roomName){
        Room room = roomRepository.findRoomByName(roomName);
        HashMap um = (HashMap) roomMap.get(room);
        this.userMap = um;
        synchronized (userMap){
            userMap.put(user, user.printWriter);
        }
        System.out.println(room.getRoomName() + " 에 입장하였습니다.");
    }

    // 방 퇴장
    public void exitRoom(){
        synchronized (userMap){
            userMap.remove(user);
        }
    }

    //상태에 따른 구분 필요
    public void chat(User user, String content){
        synchronized (userMap){
            Collection collection = userMap.values();
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
                PrintWriter pw = (PrintWriter) iter.next();
                pw.println(content);
                pw.flush();
            }
        }
    }

}
