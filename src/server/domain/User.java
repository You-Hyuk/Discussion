package server.domain;

import java.io.PrintWriter;
import java.io.Serializable;

public class User implements Serializable {
    public String userName;
    public PrintWriter printWriter;

    public User(String userName, PrintWriter printWriter) {
        this.userName = userName;
        this.printWriter = printWriter;
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
}
