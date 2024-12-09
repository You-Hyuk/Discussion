package server.domain;

import java.io.PrintWriter;
import java.io.Serializable;

public class User implements Serializable {
    private String userName;
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

    public PrintWriter getPrintWriter() {
        return printWriter;
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

}
