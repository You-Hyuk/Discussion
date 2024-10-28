package server.repository;

import server.domain.Room;

import java.io.*;
import java.util.ArrayList;

public class RoomRepository {

    private FileInputStream fis;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private FileOutputStream fos;

    private final String ROOM_FILE = "./data/room.txt";
    public Room findRoomByName(String roomName) {
        try{
            ArrayList<Room> rooms = readRoom();
            for (Room room : rooms) {
                if(room.getRoomName().equals(roomName))
                    return room;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("해당 이름의 채팅방이 존재하지 않습니다.");
        return null;
    }

    public RoomRepository() {
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
        try (FileInputStream fis = new FileInputStream(ROOM_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            rooms = (ArrayList<Room>) ois.readObject();

        } catch (EOFException eof) {
            // EOFException 발생 시 빈 리스트로 초기화
            System.out.println("파일이 비어 있거나 처음 생성된 상태입니다.");
            rooms = new ArrayList<>();  // 빈 리스트 반환
        } catch (FileNotFoundException fnf) {
            // 파일이 없을 경우 새로운 리스트를 생성
            System.out.println("파일이 없습니다. 새 파일을 생성합니다.");
            rooms = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }
}
