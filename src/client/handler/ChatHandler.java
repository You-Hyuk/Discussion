package client.handler;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class ChatHandler {
    private ObjectInputStream ois;
    private FileInputStream fis;

    public ChatHandler(ObjectInputStream ois, FileInputStream fis){
        this.ois = ois;
        this.fis = fis;
    }


}
