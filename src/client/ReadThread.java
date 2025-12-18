
import java.io.*;
import java.net.*;

import common.Message;
 
public class ReadThread extends Thread {
    private ObjectInputStream reader;
    private Socket socket;

    public ReadThread(Socket Socket, ObjectInputStream reader) {
        socket = Socket;
        this.reader = reader;
    }

    public void run() {
        while (true) {
            try {
                Message msg = (Message) reader.readObject();

                switch (msg.getType()) {

                    case MESSAGE:
                        System.out.println(msg.getContent());
                        break;

                    case ERROR:
                        System.out.println("ERROR: " + msg.getContent());
                        break;

                    case SUCCESS:
                        System.out.println("SUCCESS: " + msg.getContent());
                        socket.close();
                        return;
                }
            } catch (IOException e) {
                System.err.println("Fatal IO error: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("Fatal Class error: " + e.getMessage());
            }
        }
    }
}