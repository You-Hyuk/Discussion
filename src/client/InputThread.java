package client;

import java.io.BufferedReader;
import java.net.Socket;

public class InputThread extends Thread {
    private Socket sock = null;
    private BufferedReader br = null;

    public InputThread(Socket sock, BufferedReader br) {
        this.sock = sock;
        this.br = br;
    }

    public void run() {
        try {
            String line = null;

            //println()만을 실행, 즉 화면을 출력하는 것만 무한반복
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (br != null) br.close();
                if (sock != null) sock.close();
            } catch (Exception ex) {
            }
        }
    }
}
