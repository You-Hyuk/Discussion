package server.application;

import server.controller.ChatController;
import server.controller.RoomController;
import server.domain.Room;
import server.domain.Status;
import server.domain.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class ChatThread extends Thread {
    private Socket sock;
    private String userName;
    private BufferedReader br;  //buffer 사용 용도
    private PrintWriter pw;  //println() 사용 용도
    private User user;
    private Room room;
    private Status status;
    private HashMap<String, List<PrintWriter>> userMap;

    private ChatController chatController;
    private boolean inRoom = false;
    private boolean hasStatus = false;
    private RoomController roomController;

    public ChatThread(Socket sock, HashMap userMap) {
        this.sock = sock;
        this.userMap = userMap;
        this.roomController = new RoomController(userMap);
        this.chatController = new ChatController(userMap);
        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            //클라이언트가 접속 되면 id를 전송한다는 프로토콜을 정의했기 때문에 readLine()을 통해 id를 받는다
            userName = br.readLine();
            user = new User(userName, pw);
            System.out.println("userName = " + userName);

            chatController.printRoomList(user);
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

                if(line.split(" ")[0].equals("/exit") && inRoom) {
                    chatController.exitRoom(room, user);
                    roomController.removeUserFromRoom(room.getRoomName(), user.getPrintWriter());
                    chatController.sendToClient(user, user.getUserName() + " 님이 " + room.getRoomName() + "방을 퇴장하였습니다.");
                    inRoom = false;
                }

                if(inRoom && hasStatus){
                    chatController.chat(room, user, status, line);
                }

                //Status 선택 메소드 추가 필요
                if(line.split(" ")[0].equals("/e")){
                    String[] s = line.split(" ");
                    String roomName = s[1];
                    chatController.selectStatus(roomName, user);
                    String selectStatus = br.readLine();
                    if (selectStatus.equals("1")){
                        status = Status.STATUS1;
                        this.hasStatus = true;
                    }
                    if (selectStatus.equals("2")){
                        status = Status.STATUS2;
                        this.hasStatus = true;
                    }
                    if (selectStatus.equals("3")){
                        status = Status.NONE;
                    }
                    Room enteredRoom = chatController.enterRoom(roomName, user);
                    roomController.addUserToRoom(roomName, user.getPrintWriter());
                    inRoom = true;
                    room = enteredRoom;

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