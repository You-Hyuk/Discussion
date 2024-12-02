package server.domain;
//printwriter 직렬화 대상에서 제외, initializePrintWriter 메소드 추가
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;

public class User implements Serializable {
    private String userName;
    //private PrintWriter printWriter;
    private transient PrintWriter printWriter; // 직렬화 대상에서 제외
    private String status;

    public User(String userName, PrintWriter printWriter) {
        this.userName = userName;
        this.printWriter = printWriter;
        this.status = "중립"; // 기본 상태는 중립
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public void initializePrintWriter(Socket socket) throws IOException {
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
    }
}
