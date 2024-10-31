package server.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomController {
    private Map<String, List<PrintWriter>> userMap;

    public RoomController(Map<String, List<PrintWriter>> userMap) {
        this.userMap = userMap;
    }

    // 방에 유저 추가
    public void addUserToRoom(String roomName, PrintWriter writer) {
        // 해당 방에 유저 리스트가 없으면 새로 생성
        userMap.putIfAbsent(roomName, new ArrayList<>());
        userMap.get(roomName).add(writer);
    }

    // 방에서 유저 제거
    public void removeUserFromRoom(String roomName, PrintWriter writer) {
        List<PrintWriter> writers = userMap.get(roomName);
        if (writers != null) {
            writers.remove(writer);
            if (writers.isEmpty()) {
                userMap.remove(roomName);  // 방에 유저가 없으면 방 제거
            }
        }
    }
}
