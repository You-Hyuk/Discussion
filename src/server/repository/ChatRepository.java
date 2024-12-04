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
        System.out.println("생성된 채팅방 파일 이름: " + room.getChatFileName());
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
            System.out.println("Room Name: " + room.getRoomName());
            System.out.println("Chat(username, message, status): " + chat);
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

    public ArrayList<Chat> findChatHistory(Room room) {
        ArrayList<Chat> chatHistory = new ArrayList<>(); // 빈 리스트로 초기화
        try {
            chatHistory = readChatHistory(room); // 채팅 기록 읽기
            System.out.println("findChatHistory에서 readChatHistory 결과 확인:" + chatHistory);
        } catch (Exception e) {
            e.printStackTrace(); // 예외 스택 트레이스 출력
        }
        if (chatHistory.isEmpty()) {
            System.out.println("해당 채팅방의 채팅 기록이 존재하지 않습니다.");
        }
        return chatHistory; // 항상 빈 리스트 또는 채팅 기록 반환
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
        System.out.println("readChatHistory에서 room 확인: " + room);
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
            System.out.println("chatrepository에서 출력: 파일이 없습니다. 새 파일을 생성합니다.");
            chatHistory = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chatHistory;
    }

    private void writeChatHistory(String filePath, ArrayList<Chat> chats) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(chats);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteChatLog(String roomName) {
        File chatLog = new File(DIRECTORY_PATH+ roomName + ".txt");
        if (chatLog.exists()) {
            if (!chatLog.delete()) {
                System.out.println("채팅기록 파일 삭제 실패: " + roomName);
            }
        }
    }

    public void updateLikeCount(Room room, String chatId) {
        try {
            if (room == null) {
                System.err.println("Room is null. Cannot update like.");
                return;
            }
            if (chatId == null) {
                System.err.println("Chat ID is null. Cannot update like.");
                return;
            }
            String filePath = DIRECTORY_PATH + room.getRoomName() + ".txt";
            ArrayList<Chat> chats = readChatHistory(room);
            if (chats == null) {
                System.err.println("Chat history is null. Cannot update like count.");
                return;
            }
            boolean updated = false;
            for (Chat chat : chats) {
                if (chat.getId().equals(chatId)) {
                    chat.incrementLike(); // 좋아요 수 증가
                    updated = true;
                    break;
                }
            }
            if (updated) {
                writeChatHistory(filePath, chats); // 업데이트된 기록 저장
                System.out.println("좋아요 업데이트 완료: Chat ID = " + chatId);
            } else {
                System.err.println("Chat ID not found: " + chatId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Chat findMostLikedChat(ArrayList<Chat> chats) {
        if (chats == null || chats.isEmpty()) {
            return null;
        }
        // 좋아요 수 기준으로 가장 높은 채팅 찾기
        Chat mostLikedChat = chats.get(0);
        for (Chat chat : chats) {
            if (chat.getLike() > mostLikedChat.getLike()) {
                mostLikedChat = chat;
            }
        }
        return mostLikedChat;
    }
}
