package server.repository;

import server.domain.Chat;
import server.domain.Room;

import java.io.*;

public class ChatRepository {

    private FileInputStream fis;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private FileOutputStream fos;

    public void createChatFile(Room room){
        String roomName = room.getRoomName();
        File file = new File("./data/" + roomName + ".txt");
    }

    public void saveChat(Room room, Chat chat){
        try {
            String filePath = room.getRoomName() + ".txt";
            fos = new FileOutputStream(filePath); //InputThread에서 BufferedReader로 읽은 line을 저장하기 위해 Writer 사용
            oos = new ObjectOutputStream(fos);
            String chatHistory = chat.toString();
            oos.writeObject(chatHistory);
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
}
