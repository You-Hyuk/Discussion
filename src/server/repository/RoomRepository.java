package server.repository;

import server.domain.Room;
import server.domain.User;

import java.io.*;
import java.util.ArrayList;

public class RoomRepository {

    private ObjectOutputStream oos;
    private FileOutputStream fos;

    private final String ROOM_FILE = "src/server/data/room.txt";

    public Room findRoomByName(String roomName) {
        try{
            System.out.println("findroombyname 호출");
            ArrayList<Room> rooms = readRoom();
            System.out.println("roomrepository.findroombyname에서 readroom 확인: " + rooms);
            for (Room room : rooms) {
                if(room.getRoomName().equals(roomName)) {
                    return room;
                }
            }
        }catch (Exception e){
            //e.getMessage();
        }
        System.out.println("해당 이름의 채팅방이 존재하지 않습니다.");
        return null;
    }

    public ArrayList<Room> getRoomList(){
        ArrayList<Room> rooms = readRoom();
        return rooms;
    }

    // 채팅방 안의 User를 반환
    public User findUserByName(Room room, String userName){
        ArrayList<User> userList = room.getUserList();
        for (User user : userList) {
            if (user.getUserName().equals(userName)){
                return user;
            }
        }
        System.out.println(userName + " 에 일치하는 User가 존재하지 않습니다");
        return null;
    }

    public Room addUserToRoom(String roomName, User user){
        ArrayList<Room> rooms = readRoom(); // 파일에서 Room 리스트 읽기
        Room room1 = findRoomByName(roomName); // Room 객체 찾기
        if (room1 == null) {
            System.out.println("Room not found: " + roomName);
            return null; // 방이 존재하지 않으면 null 반환
        }

        // 사용자 상태에 따라 찬성/반대 수 증가
        if (user.getStatus() != null) {
            if (user.getStatus().equals(room1.getFirstStatus())) {
                room1.incrementFirstStatusCount(); // 찬성 수 증가
            } else if (user.getStatus().equals(room1.getSecondStatus())) {
                room1.incrementSecondStatusCount(); // 반대 수 증가
            }
        }

        // 방에 사용자 추가
        room1.addUser(user);

        // Room 리스트를 업데이트
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomName().equals(roomName)) {
                rooms.set(i, room1); // 업데이트된 Room 객체로 교체
                break;
            }
        }

        // 파일에 저장
        try {
            fos = new FileOutputStream(ROOM_FILE);
            oos = new ObjectOutputStream(fos);
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

        return room1;
    }


    public void createRoom(Room room){
        try{
            // 기존 Room 리스트를 불러옴
            ArrayList<Room> rooms = readRoom();
            // 새로운 Room 추가
            rooms.add(room);

            fos = new FileOutputStream(ROOM_FILE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(rooms);
        }catch (Exception e){
            e.getMessage();
        }
    }

    public ArrayList<Room> readRoom() {
        ArrayList<Room> rooms = null;
        try  {
            FileInputStream fis = new FileInputStream(ROOM_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);

            rooms = (ArrayList) ois.readObject();

        } catch (EOFException eof) {
            // EOFException 발생 시 빈 리스트로 초기화
            System.out.println("파일이 비어 있거나 처음 생성된 상태입니다.");
            rooms = new ArrayList<>();  // 빈 리스트 반환
        } catch (FileNotFoundException fnf) {
            // 파일이 없을 경우 새로운 리스트를 생성
            System.out.println("파일이 없습니다. 새 파일을 생성합니다.");
            rooms = new ArrayList<>();
        } catch (Exception e) {
            e.getMessage();
        }
        return rooms;
    }

    public void deleteExpiredRooms() {
        try {
            ArrayList<Room> rooms = readRoom();
            ArrayList<Room> updatedRooms = new ArrayList<>();
            long currentTime = System.currentTimeMillis();

            for (Room room : rooms) {
                // 방의 Timestamp 확인
                if (currentTime - room.getTimestamp().getTime() >= 24 * 60 * 60 * 1000) {
                    System.out.println(room.getRoomName() + " 방이 삭제되었습니다 (24시간 초과).");
                    ChatRepository chatRepository = new ChatRepository();
                    chatRepository.deleteChatLog(room.getRoomName());
                } else {
                    // 24시간이 지나지 않은 방은 유지
                    updatedRooms.add(room);
                }
            }

            // 업데이트된 방 목록 저장
            fos = new FileOutputStream(ROOM_FILE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(updatedRooms);
            oos.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
