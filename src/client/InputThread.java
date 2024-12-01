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

    private void receiveMessages() {
        try {
            String line;
            while ((line = br.readLine()) != null) {
                // 메시지 처리 로직
                if (line.startsWith("STATUS_UPDATE: ")) {
                    // 상태 업데이트 메시지 처리
                    handleStatusUpdate(line.replace("STATUS_UPDATE: ", ""));
                } else {
                    // 일반 메시지 출력
                    System.out.println(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleStatusUpdate(String statusMessage) {
        String[] parts = statusMessage.split(",");
        String roomName = parts[0];
        int firstStatusCount = Integer.parseInt(parts[1]);
        int secondStatusCount = Integer.parseInt(parts[2]);

        System.out.println("Status Update:");
        System.out.println("Room: " + roomName);
        System.out.println("찬성: " + firstStatusCount);
        System.out.println("반대: " + secondStatusCount);

        // 여기서 UI 클래스 호출 또는 상태 업데이트 로직 추가 가능
    }
}
