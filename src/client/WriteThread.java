
import java.io.*;
import java.net.*;
import java.util.Scanner;

import common.Message;

public class WriteThread extends Thread {
    private ObjectOutputStream objectOut;
 
    public WriteThread(Socket socket, ObjectOutputStream out) {
        objectOut = out;
    }
 
    public void run() {
      Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                String input = scanner.nextLine();
                objectOut.writeObject(new Message(Message.Type.RESPONSE, input));
                objectOut.flush();
            } catch (IOException e) {
                System.err.println("Fatal IO error: " + e.getMessage());
                break;
            }
        }
    }
}