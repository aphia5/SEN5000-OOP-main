package server;

import common.*;
import common.Record;
import utils.Validator;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private final CSVManager csvManager;
    
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private State state = State.ASK_ID;

    // store inputs across the dialog
    private String userId = null;
    private String postcode = null;
    private double co2Reading = 0.0;

    public ClientHandler(Socket clientSocket, CSVManager csvManager) {
        this.clientSocket = clientSocket;
        this.csvManager = csvManager;
    }
    
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            sendMessage("Welcome to the application!");

            sendRequest("To start please enter your User ID: ");

            do {
                Message msg = (Message) in.readObject();
                handleMessage(msg);
            } while (state != State.COMPLETE);

            
            out.writeObject(new Message(Message.Type.CLOSE, ""));
            out.flush();

            closeConnection();

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

    private void sendSuccess(String text) throws IOException {
        out.writeObject(new Message(Message.Type.SUCCESS, text));
        out.flush();
    }

    private void handleMessage(Message msg) throws IOException {

        String input = msg.getContent();

        switch (state) {

            case ASK_ID:
                if (Validator.isValidUserId(input)) {
                    // store user id and move to next state
                    userId = input.trim();
                    state = State.ASK_POSTCODE;
                    sendRequest("Please enter your postcode: ");
                } else {
                    sendError("ID must be numeric");
                    sendRequest("Please enter your User Id: ");
                }
                break;

            case ASK_POSTCODE:
                if (Validator.isValidPostcode(input)) {
                    // store postcode and move to next state
                    postcode = input.trim();
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
                        co2Reading = co2;

                        // Create a record and write to CSV via CSVManager
                        Record rec = new Record(userId, postcode, co2Reading);
                        boolean wrote = csvManager.writeRecord(rec);

                        if (wrote) {
                            sendSuccess("Data accepted and logged");
                            state = State.COMPLETE;
                        } else {
                            sendError("Data accepted but failed to write to database");
                            sendRequest("Retry entering CO2 reading (in ppm): ");
                        }
                        
                    } else {
                        sendError("CO2 out of range");
                        sendRequest("Please enter the CO2 reading (in ppm): ");
                    }
                } catch (NumberFormatException e) {
                    sendError("CO2 must be a number");
                    sendRequest("Please enter the CO2 reading (in ppm): ");
                }
                break;

            case COMPLETE:
                break;
        }
    }

    private void closeConnection() {
        try {
            closeResources();
            System.out.println("[ClientHandler] Connection closed for user: " + userId);
        } catch (IOException e) {
            System.err.println("[ClientHandler] Error closing resources: " + e.getMessage());
        }
    }

    private void closeResources() throws IOException {
        IOException ex = null;
        try {
            if (in != null) in.close();
        } catch (IOException e) { ex = e; }
        try {
            if (out != null) out.close();
        } catch (IOException e) { ex = e; }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) { ex = e; }
        if (ex != null) throw ex;
    }
}
