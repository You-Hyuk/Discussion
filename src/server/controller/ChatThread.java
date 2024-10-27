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

    public ChatThread(Socket sock, HashMap roomMap, HashMap userMap) {
        this.sock = sock;
        this.roomMap = roomMap;
        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            //클라이언트가 접속 되면 id를 전송한다는 프로토콜을 정의했기 때문에 readLine()을 통해 id를 받는다
            nickname = br.readLine();
            this.user = new User(nickname, pw);
//            broadcast(id + "님이 접속하였습니다.");

            //임계 영역을 통한 동기화 문제 해결
//            synchronized (hm) {
//                hm.put(this.nickName, pw);
//            }
            initFlag = true;
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void run() {
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                broadcast(line);
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

    // 방 입장
    public void enterRoom(Room room){
        synchronized (userMap){
            userMap.put(user, user.printWriter);
        }
    }

    // 방 퇴장
    public void exitRoom(){
        synchronized (userMap){
            userMap.remove(user);
        }
    }
}