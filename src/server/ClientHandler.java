package server;

import common.*;
import utils.Validator;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    //private final CSVManager csvManager;
    
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private State state = State.ASK_ID;

    public ClientHandler(Socket clientSocket, CSVManager csvManager) {
        this.clientSocket = clientSocket;
        //this.csvManager = csvManager;
    }
    
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            sendMessage("Welcome to the application!");

            while (state != State.COMPLETE) {
                Message msg = (Message) in.readObject();
                handleMessage(msg);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(String text) throws IOException {
        out.writeObject(new Message(Message.Type.REQUEST, text));
        out.flush();
    }

    private void sendMessage(String text) throws IOException {
        out.writeObject(new Message(Message.Type.MESSAGE, text));
        out.flush();
    }

    private void sendError(String text) throws IOException {
        out.writeObject(new Message(Message.Type.ERROR, text));
        out.flush();
    }

    private void handleMessage(Message msg) throws IOException {

        String input = msg.getContent();

        switch (state) {

            case ASK_ID:
                if (Validator.isValidUserId(input)) {
                    state = State.ASK_POSTCODE;
                    sendRequest("Please enter your postcode: ");
                } else {
                    sendError("ID must be numeric");
                    sendRequest("Please enter your User Id:");
                }
                break;

            case ASK_POSTCODE:
                if (Validator.isValidPostcode(input)) {
                    state = State.ASK_CO2;
                    sendRequest("Please enter the CO2 reading (in ppm): ");
                } else {
                    sendError("Invalid postcode");
                    sendRequest("Please enter your postcode: ");
                }
                break;

            case ASK_CO2:
                try {
                    double co2 = Double.parseDouble(input);
                    if (Validator.isValidCo2Reading(co2)) {
                        out.writeObject(new Message(Message.Type.SUCCESS, "Data accepted"));
                        out.flush();
                        state = State.COMPLETE;
                    } else {
                        sendError("CO2 out of range");
                        sendRequest("Please enter the CO2 reading (in ppm): ");
                    }
                } catch (NumberFormatException e) {
                    sendError("CO2 must be a number");
                    sendRequest("Please enter the CO2 reading (in ppm): ");
                }
                break;
        }
    }
}
