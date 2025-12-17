package server;

import common.Record;
import utils.Validator;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * ClientHandler manages communication with a single connected client.
 * Implements Runnable to run in a separate thread, allowing the server
 * to handle multiple clients simultaneously.
 * 
 * Each client connection is handled independently in its own thread.
 * 
 * @author CO2 Monitoring System Team
 * @version 1.0
 */
public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private final CSVManager csvManager;
    private final int clientId;
    
    private BufferedReader input;
    private PrintWriter output;
    
    /**
     * Constructor for ClientHandler.
     * 
     * @param clientSocket The socket connection to the client
     * @param csvManager The CSV manager for writing records
     * @param clientId Unique identifier for this client connection
     */
    public ClientHandler(Socket clientSocket, CSVManager csvManager, int clientId) {
        this.clientSocket = clientSocket;
        this.csvManager = csvManager;
        this.clientId = clientId;
    }
    
    /**
     * Main thread execution method.
     * Handles the entire client communication lifecycle.
     */
    @Override
    public void run() {
        try {
            // Initialize input/output streams
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            
            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            System.out.println("[Server] Client #" + clientId + " connected from: " + clientAddress);
            
            // Send welcome message
            sendMessage("WELCOME|CO2 Monitoring System Server");
            sendMessage("INFO|Please submit your CO2 reading data");
            
            // Main communication loop - allow multiple submissions
            boolean continueSession = true;
            while (continueSession && !clientSocket.isClosed()) {
                
                // Prompt for data
                sendMessage("PROMPT|Ready to receive data. Format: DATA|userId|postcode|co2Reading");
                
                // Read client input
                String clientMessage = input.readLine();
                
                if (clientMessage == null) {
                    // Client disconnected
                    System.out.println("[Server] Client #" + clientId + " disconnected");
                    break;
                }
                
                System.out.println("[Server] Client #" + clientId + " sent: " + clientMessage);
                
                // Process the message
                if (clientMessage.startsWith("DATA|")) {
                    handleDataSubmission(clientMessage);
                } else if (clientMessage.equalsIgnoreCase("EXIT") || 
                          clientMessage.equalsIgnoreCase("QUIT")) {
                    sendMessage("INFO|Goodbye! Closing connection.");
                    continueSession = false;
                } else {
                    sendMessage("ERROR|Invalid message format. Use: DATA|userId|postcode|co2Reading");
                }
            }
            
        } catch (SocketException e) {
            System.out.println("[Server] Client #" + clientId + " connection lost: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[Server] ERROR with Client #" + clientId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }
    
    /**
     * Processes a data submission from the client.
     * Format: DATA|userId|postcode|co2Reading
     * 
     * @param message The data message from client
     */
    private void handleDataSubmission(String message) {
        try {
            // Parse the message
            String[] parts = message.split("\\|");
            
            if (parts.length != 4) {
                sendMessage("ERROR|Invalid data format. Expected: DATA|userId|postcode|co2Reading");
                return;
            }
            
            String userId = parts[1].trim();
            String postcode = parts[2].trim();
            String co2String = parts[3].trim();
            
            // Validate User ID
            if (!Validator.isValidUserId(userId)) {
                sendMessage("ERROR|" + Validator.getUserIdError(userId));
                return;
            }
            
            // Validate Postcode
            if (!Validator.isValidPostcode(postcode)) {
                sendMessage("ERROR|" + Validator.getPostcodeError(postcode));
                return;
            }
            
            // Parse and validate CO2 reading
            double co2Reading;
            try {
                co2Reading = Validator.parseCo2Reading(co2String);
            } catch (NumberFormatException e) {
                sendMessage("ERROR|" + e.getMessage());
                return;
            }
            
            if (!Validator.isValidCo2Reading(co2Reading)) {
                sendMessage("ERROR|" + Validator.getCo2ReadingError(co2Reading));
                return;
            }
            
            // All validations passed - create record
            Record record = new Record(userId, postcode, co2Reading);
            
            // Write to CSV (thread-safe)
            boolean success = csvManager.writeRecord(record);
            
            if (success) {
                sendMessage("SUCCESS|Data recorded successfully");
                sendMessage("INFO|Timestamp: " + record.getTimestampString());
                sendMessage("INFO|Total records in database: " + csvManager.getRecordCount());
                System.out.println("[Server] Client #" + clientId + " data saved: " + record.getUserId());
            } else {
                sendMessage("ERROR|Failed to save data to database");
            }
            
        } catch (Exception e) {
            sendMessage("ERROR|Server error processing data: " + e.getMessage());
            System.err.println("[Server] ERROR processing client #" + clientId + " data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sends a message to the client.
     * 
     * @param message The message to send
     */
    private void sendMessage(String message) {
        if (output != null && !clientSocket.isClosed()) {
            output.println(message);
        }
    }
    
    /**
     * Closes all resources associated with this client connection.
     */
    private void closeConnection() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            System.out.println("[Server] Client #" + clientId + " connection closed");
        } catch (IOException e) {
            System.err.println("[Server] ERROR closing client #" + clientId + " connection: " + e.getMessage());
        }
    }
    
    /**
     * Gets the client ID.
     * 
     * @return The unique client identifier
     */
    public int getClientId() {
        return clientId;
    }
}