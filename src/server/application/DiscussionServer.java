package server.application;

import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.List;

public class DiscussionServer {

    public static void main(String[] args) {
        try {
            HashMap<String, List<PrintWriter>> userMap = new HashMap<>();
            ServerSocket server = new ServerSocket(10001);
            System.out.println("접속을 기다립니다.");


            //메인스레드는 이 동작만 무한 루프를 돌린다
            while (true) {
                //클라이언트가 요청하기 전까지 Block
                //accept 상태에서 요청을 받는다면 Socket을 반환
                Socket sock = server.accept();
                ChatThread chatThread = new ChatThread(sock, userMap);
                chatThread.start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
