package client;

import java.net.*;
import java.io.*;

public class DiscussionClient {
    public static void main(String[] args) {
        Socket sock = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        FileOutputStream fos = null;
        boolean endflag = false;
        try {
            sock = new Socket("192.168.35.48", 10001);

            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));


            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("ID를 입력하세요: ");
            pw.println(keyboard.readLine());

            pw.flush();

            InputThread it = new InputThread(sock, br);
            it.start();

            String line = null;
            while ((line = keyboard.readLine()) != null) {
                pw.println(line);
                pw.flush();
                if (line.equals("/quit")) {
                    endflag = true;
                    break;
                }

                if(line.split(" ")[0].equals("/e")){
                    String selectStatus = null;
                    if ((selectStatus = keyboard.readLine()) != null){
                        if (selectStatus.equals("1") || selectStatus.equals("2") || selectStatus.equals("3")) {
                            pw.println(selectStatus);
                            pw.flush();
                        }
                    }
                }
            }

            System.out.println("클라이언트의 접속을 종료합니다.");
        } catch (Exception ex) {
            if (!endflag) {
                System.out.println(ex);
            }
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }
}