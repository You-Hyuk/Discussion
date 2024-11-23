package server.repository;

import server.domain.Chat;
import server.domain.Room;

import java.io.*;
import java.util.ArrayList;

public class ChatRepository {

    private ObjectOutputStream oos;
    private FileOutputStream fos;

    private final String DIRECTORY_PATH = "src/server/data/";

    public void createChatFile(Room room){
        // roomName을 안전한 이름으로 변환
//        String roomName = room.getRoomName().replaceAll("[^a-zA-Z0-9._-]", "_");
//        String fileName = room.getRoomName().replace("\"", "") + ".txt";
        String fileName = room.getRoomName() + ".txt";
        String path = DIRECTORY_PATH + fileName;
        // data 디렉토리 생성 (존재하지 않을 때만)
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs(); // 디렉토리가 없으면 생성
        }

        // 파일 생성
        File file = new File(path);
        try {
            if (file.createNewFile()) { // 파일이 존재하지 않으면 새로 생성
                System.out.println("파일이 생성되었습니다: " + file.getName());
            } else {
                System.out.println("파일이 이미 존재합니다.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        room.setChatFileName(file.getName());
        System.out.println(room.getChatFileName());
    }

    public void saveChat(Room room, Chat chat){
        try {

            if (room == null) {
                System.err.println("Room is null. Cannot save.");
                return;
            }

            if (chat == null) {
                System.err.println("Chat is null. Cannot save.");
                return;
            }

            String filePath = DIRECTORY_PATH + room.getRoomName() + ".txt";

            ArrayList<Chat> chats = readChatHistory(room);

            if (chats == null) {
                chats = new ArrayList<>(); // 채팅 기록 초기화
            }

            chats.add(chat);
            fos = new FileOutputStream(filePath); //InputThread에서 BufferedReader로 읽은 line을 저장하기 위해 Writer 사용
            oos = new ObjectOutputStream(fos);

            oos.writeObject(chats);
            System.out.println(room.getRoomName() + " 채팅 저장 완료");

            System.out.println("Room: " + room);
            System.out.println("Room Name: " + (room != null ? room.getRoomName() : "null"));
            System.out.println("Chat: " + chat);
            System.out.println("Chat History: " + chats);
            System.out.println("File Path: " + filePath);

        }catch (IOException e){
            e.getMessage();
        }finally {
            try {
                oos.close();
                fos.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Chat> findChatHistory(Room room){
        try{
            ArrayList<Chat> chatHistory = readChatHistory(room);
            return chatHistory;
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("해당 채팅방의 채팅 기록이 존재하지 않습니다.");
        return null;
    }

    public ArrayList<Chat> readChatHistory(Room room) {

        if (room == null) {
            System.err.println("Room is null. Returning empty chat history.");
            return new ArrayList<>();
        }

        if (room.getChatFileName() == null) {
            System.err.println("Chat file name is null. Returning empty chat history.");
            return new ArrayList<>();
        }

        ArrayList<Chat> chatHistory = null;
        String chatFileName = room.getChatFileName();
        String path = DIRECTORY_PATH + chatFileName;

        try{
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);

            chatHistory = (ArrayList<Chat>) ois.readObject();

        } catch (EOFException eof) {
            // EOFException 발생 시 빈 리스트로 초기화
            System.out.println("파일이 비어 있거나 처음 생성된 상태입니다.");
            chatHistory = new ArrayList<>();  // 빈 리스트 반환
        } catch (FileNotFoundException fnf) {
            // 파일이 없을 경우 새로운 리스트를 생성
            System.out.println("파일이 없습니다. 새 파일을 생성합니다.");
            chatHistory = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chatHistory;
    }
}