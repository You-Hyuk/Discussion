package server.controller;

import server.domain.Room;
import server.domain.User;
import server.repository.RoomRepository;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomController {
    private Map<String, List<PrintWriter>> userMap;
    private RoomRepository roomRepository;

    public RoomController(Map<String, List<PrintWriter>> userMap) {
        this.userMap = userMap;
        this.roomRepository = new RoomRepository();
    }

    // 방에 유저 추가
    public void addUserToRoom(String roomName, PrintWriter writer) {
        // 해당 방에 유저 리스트가 없으면 새로 생성
        userMap.putIfAbsent(roomName, new ArrayList<>());
        userMap.get(roomName).add(writer);
    }

    // 방에서 유저 제거
    public void removeUserFromRoom(String roomName, User user) {
        Room room = roomRepository.findRoomByName(roomName);
        List<PrintWriter> writers = userMap.get(roomName);
        if (writers != null) {
            writers.remove(user.getPrintWriter());
            if (writers.isEmpty()) {
                userMap.remove(roomName);  // 방에 유저가 없으면 방 제거
            }
        }
        room.removeUser(user);
    }

    public List<Room> getRoomList() {
        return roomRepository.readRoom(); // room.txt에서 방 목록 읽기
    }

    public Room findRoomByName(String roomName){
        Room room = roomRepository.findRoomByName(roomName);
        return room;
    }

    public Room enterRoom(String roomName, User user) {
        Room room = roomRepository.findRoomByName(roomName);
        room = roomRepository.addUserToRoom(roomName, user); // addUserToRoom 호출
        userMap.putIfAbsent(roomName, new ArrayList<>());
        userMap.get(roomName).add(user.getPrintWriter());
        return room;
    }

    public void vote(String roomName, String status) {
        roomRepository.voteDiscussion(roomName,status);
    }
}