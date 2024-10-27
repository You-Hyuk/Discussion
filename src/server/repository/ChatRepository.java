package server.repository;

import server.domain.Chat;
import server.domain.Room;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ChatRepository {
    FileWriter fw = null;

    public void createChatFile(Room room){
        String roomName = room.getRoomName();
        File file = new File(roomName + ".txt");
    }

    public void saveChat(Room room, Chat chat){
        try {
            String filePath = room.getRoomName() + ".txt";
            fw = new FileWriter(filePath); //InputThread에서 BufferedReader로 읽은 line을 저장하기 위해 Writer 사용
            String chatHistory = chat.toString();
            fw.write(chatHistory);
        }catch (IOException e){
            e.getMessage();
        }finally {
            try {
                fw.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
