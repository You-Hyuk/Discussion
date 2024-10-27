package server.controller;

import server.domain.Room;
import server.domain.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ChatThread extends Thread {
    private Socket sock;
    private String nickname;
    private BufferedReader br;  //buffer 사용 용도
    private PrintWriter pw;  //println() 사용 용도
    private HashMap userMap; //방 안에서의 Stream Map
    private HashMap roomMap; //방 관리
    private boolean initFlag = false;
    private User user;
    private ChatController chatController;

    public ChatThread(Socket sock, HashMap roomMap, HashMap userMap) {
        this.sock = sock;
        this.roomMap = roomMap;
        this.userMap = userMap;
        this.chatController = new ChatController(roomMap, userMap);
        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            //클라이언트가 접속 되면 id를 전송한다는 프로토콜을 정의했기 때문에 readLine()을 통해 id를 받는다
            nickname = br.readLine();
            this.user = new User(nickname, pw);

            initFlag = true;
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void run() {
        try {
            String line = null;
            Room room;
            while ((line = br.readLine()) != null) {
//                broadcast(line);
                if(line.startsWith("/c")){
                    String[] s = line.split(" ");
                    String roomName = s[1];
                    String firstStatus = s[2];
                    String secondStatus = s[3];
                    String nickName = user.getNickname();
                    room = new Room(roomName, firstStatus, secondStatus, nickname);
                    chatController.createRoom(room);
                }

                if(line.startsWith("/e")){
                    String roomName = line.substring(3);
                    System.out.println(roomName);
                    userMap = (HashMap) roomMap.get(roomName);
                    chatController.enterRoom(roomName);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            synchronized (userMap) {
                userMap.remove(user);
            }
            try {
                if (sock != null)
                    sock.close();
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    //HashMap의 value값을 이용하여 접속한 모든 유저의 OutputStream 사용
    public void broadcast(String msg) {
        synchronized (userMap) {
            Collection collection = userMap.values();
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
                PrintWriter pw = (PrintWriter) iter.next();
                pw.println(msg);
                pw.flush();
            }
        }
    }
}