package server.domain;

import java.io.PrintWriter;

public class User {
    public String nickname;
    public PrintWriter printWriter;

    public User(String nickname, PrintWriter printWriter) {
        this.nickname = nickname;
        this.printWriter = printWriter;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }
}
