package server.controller;

import server.domain.Room;
import server.domain.Status;
import server.domain.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatThread extends Thread {
    private Socket sock;
    private String userName;
    private BufferedReader br;  //buffer 사용 용도
    private PrintWriter pw;  //println() 사용 용도
    private boolean initFlag = false;
    private User user;
    private ChatController chatController;
    private boolean inRoom = false;
    private Room room;
    private Status status = Status.STATUS1; //변경 필요

    public ChatThread(Socket sock) {
        this.sock = sock;
        this.chatController = new ChatController();
        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            //클라이언트가 접속 되면 id를 전송한다는 프로토콜을 정의했기 때문에 readLine()을 통해 id를 받는다
            this.userName = br.readLine();
            this.user = new User(userName, pw);
            System.out.println("userName = " + userName);
            initFlag = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                if(line.split(" ")[0].equals("/c")){
                    String[] s = line.split(" ");
                    String roomName = s[1];
                    String firstStatus = s[2];
                    String secondStatus = s[3];
                    Room room = new Room(roomName, firstStatus, secondStatus, userName);
                    chatController.createRoom(room);
                    chatController.sendToClient(user, user.getUserName() + " 님이 " + roomName + " 토론 채팅방을 생성하였습니다.");
                }

                //Status 선택 메소드 추가 필요
                if(line.split(" ")[0].equals("/e")){
                    String[] s = line.split(" ");
                    String roomName = s[1];
                    Room enteredRoom = chatController.enterRoom(roomName, user);
                    this.inRoom = true;
                    this.room = enteredRoom;
                }

                if(line.split(" ")[0].equals("/chat") && inRoom){
                    String message = line.substring(6);
                    System.out.println(message);
                    chatController.chat(room, userName, status, message);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            try {
                if (sock != null)
                    sock.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}