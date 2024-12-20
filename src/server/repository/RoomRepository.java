package server.repository;

import server.domain.Room;
import server.domain.User;

import java.io.*;
import java.util.ArrayList;

public class RoomRepository {

    private ObjectOutputStream oos;
    private FileOutputStream fos;

    private final String ROOM_FILE = "src/server/data/room.txt";

    // 공백을 언더스코어로 변환
    private String encodeRoomName(String roomName) {
        return roomName.replace(" ", "_");
    }

    // 언더스코어를 공백으로 복원
    private String decodeRoomName(String roomName) {
        return roomName.replace("_", " ");
    }

    public Room findRoomByName(String roomName) {
        try {
            ArrayList<Room> rooms = readRoom();
            String encodedRoomName = encodeRoomName(roomName);
            for (Room room : rooms) {
                if (encodeRoomName(room.getRoomName()).equals(encodedRoomName)) {
                    return room;
                }
            }
        } catch (Exception e) {
            // e.getMessage();
        }
        System.out.println("[System] 해당 이름의 채팅방이 존재하지 않습니다.");
        return null;
    }

    public Room addUserToRoom(String roomName, User user) {
        ArrayList<Room> rooms = readRoom(); // 파일에서 Room 리스트 읽기
        Room room = findRoomByName(roomName); // Room 객체 찾기
        if (room == null) {
            System.out.println("[System] 해당 이름의 채팅방이 존재하지 않습니다.");
            return null; // 방이 존재하지 않으면 null 반환
        }

        // 방에 사용자 추가
        room.addUser(user);

        // Room 리스트를 업데이트
        for (int i = 0; i < rooms.size(); i++) {
            if (encodeRoomName(rooms.get(i).getRoomName()).equals(encodeRoomName(roomName))) {
                rooms.set(i, room); // 업데이트된 Room 객체로 교체
                break;
            }
        }

        // 파일에 저장
        saveRoomsToFile(rooms);

        return room;
    }

    public void createRoom(Room room) {
        try {
            // 기존 Room 리스트를 불러옴
            ArrayList<Room> rooms = readRoom();
            // 새로운 Room 추가
            rooms.add(room);

            saveRoomsToFile(rooms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Room> readRoom() {
        ArrayList<Room> rooms = null;
        try {
            FileInputStream fis = new FileInputStream(ROOM_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);

            rooms = (ArrayList) ois.readObject();

            // Room 이름 복원
            for (Room room : rooms) {
                room.setRoomName(decodeRoomName(room.getRoomName()));
            }

        } catch (EOFException eof) {
            // EOFException 발생 시 빈 리스트로 초기화
            System.out.println("[System] 파일이 비어 있거나 처음 생성된 상태입니다.");
            rooms = new ArrayList<>(); // 빈 리스트 반환
        } catch (FileNotFoundException fnf) {
            System.out.println("[System] 파일이 없습니다. 새 파일을 생성합니다.");
            rooms = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public void voteDiscussion(String roomName, String status) {
        ArrayList<Room> rooms = readRoom(); // 파일에서 Room 리스트 읽기
        Room room = findRoomByName(roomName); // Room 객체 찾기

        if (room.getFirstStatus().equals(status)) {
            room.incrementFirstStatusCount();
        } else {
            room.incrementSecondStatusCount();
        }

        // Room 리스트를 업데이트
        for (int i = 0; i < rooms.size(); i++) {
            if (encodeRoomName(rooms.get(i).getRoomName()).equals(encodeRoomName(roomName))) {
                rooms.set(i, room); // 업데이트된 Room 객체로 교체
                break;
            }
        }

        // 파일에 저장
        saveRoomsToFile(rooms);
    }

    public void deleteExpiredRooms() {
        try {
            ArrayList<Room> rooms = readRoom();
            ArrayList<Room> updatedRooms = new ArrayList<>();
            long currentTime = System.currentTimeMillis();

            for (Room room : rooms) {
                // 방의 Timestamp 확인
                if (currentTime - room.getTimestamp().getTime() >= 24 * 60 * 60 * 1000) {
                    System.out.println("[System] " + room.getRoomName() + " 방이 삭제되었습니다 (24시간 초과).");
                    ChatRepository chatRepository = new ChatRepository();
                    chatRepository.deleteChatLog(room.getRoomName());
                } else {
                    // 24시간이 지나지 않은 방은 유지
                    updatedRooms.add(room);
                }
            }

            // 업데이트된 방 목록 저장
            saveRoomsToFile(updatedRooms);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRoomsToFile(ArrayList<Room> rooms) {
        try {
            fos = new FileOutputStream(ROOM_FILE);
            oos = new ObjectOutputStream(fos);

            // Room 이름 변환
            for (Room room : rooms) {
                room.setRoomName(encodeRoomName(room.getRoomName()));
            }

            oos.writeObject(rooms);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
