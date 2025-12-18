
import java.io.*;
import java.net.*;
import java.util.Scanner;

import common.Message;

public class WriteThread extends Thread {
    private ObjectOutputStream objectOut;
    private ObjectInputStream reader;
 
    public WriteThread(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        try {
            OutputStream output = socket.getOutputStream();
            objectOut = out;
            reader = in;
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                Message msg = (Message) reader.readObject();

                switch (msg.getType()) {

                    case REQUEST:
                        System.out.println("SERVER: " + msg.getContent());
                        String input = scanner.nextLine();
                        objectOut.writeObject(new Message(Message.Type.RESPONSE, input));
                        objectOut.flush();
                        break;
                    default:
                        break;

                }
            } catch (IOException e) {
                System.err.println("Fatal IO error: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("Fatal Class error: " + e.getMessage());
            }
        }
    }
}