package client;

import utils.Validator;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.util.Scanner;

/**
 * ClientApp is the client application for the CO2 Monitoring System.
 * Connects to the server and allows researchers to submit CO2 readings.
 * 
 * Usage: java client.ClientApp <host> <port>
 * Example: java client.ClientApp localhost 8080
 * 
 * @author CO2 Monitoring System Team
 * @version 1.0
 */
public class ClientApp {
    
    private static Socket socket;
    private static BufferedReader serverInput;
    private static PrintWriter serverOutput;
    private static Scanner userInput;
    
    /**
     * Main entry point for the client application.
     * 
     * @param args Command line arguments: args[0] = host, args[1] = port
     */
    public static void main(String[] args) {
        // Validate command line arguments
        if (args.length != 2) {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("Usage: java client.ClientApp <host> <port>");
            System.err.println("Example: java client.ClientApp localhost 8080");
            System.exit(1);
        }
        
        String host = args[0];
        int port;
        
        // Parse port number
        try {
            port = Integer.parseInt(args[1]);
            
            if (port < 1024 || port > 65535) {
                System.err.println("ERROR: Port number must be between 1024 and 65535");
                System.exit(1);
            }
            
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid port number. Must be an integer.");
            System.err.println("Example: java client.ClientApp localhost 8080");
            System.exit(1);
            return; // Unreachable but satisfies compiler
        }
        
        // Start the client
        startClient(host, port);
    }
    
    /**
     * Connects to the server and starts the client session.
     * 
     * @param host The server hostname or IP address
     * @param port The server port number
     */
    private static void startClient(String host, int port) {
        userInput = new Scanner(System.in);
        
        try {
            // Connect to server
            System.out.println("╔════════════════════════════════════════════════════╗");
            System.out.println("║   CO2 MONITORING SYSTEM - CLIENT APPLICATION      ║");
            System.out.println("╚════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("[Client] Connecting to server...");
            System.out.println("[Client] Host: " + host);
            System.out.println("[Client] Port: " + port);
            
            socket = new Socket(host, port);
            
            System.out.println("[Client] Connected successfully!");
            System.out.println("─────────────────────────────────────────────────────");
            
            // Initialize streams
            serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOutput = new PrintWriter(socket.getOutputStream(), true);
            
            // Read welcome messages from server
            readServerMessages();
            
            // Main client loop - allow multiple submissions
            boolean continueSession = true;
            while (continueSession) {
                System.out.println();
                continueSession = submitData();
                
                if (continueSession) {
                    System.out.println();
                    System.out.print("Submit another reading? (y/n): ");
                    String response = userInput.nextLine().trim().toLowerCase();
                    continueSession = response.equals("y") || response.equals("yes");
                }
            }
            
            // Close connection
            System.out.println("\n[Client] Thank you for using the CO2 Monitoring System");
            closeConnection();
            
        } catch (UnknownHostException e) {
            System.err.println("[Client] ERROR: Unknown host '" + host + "'");
            System.err.println("[Client] Please check the hostname or IP address");
            System.exit(1);
            
        } catch (ConnectException e) {
            System.err.println("[Client] ERROR: Cannot connect to server");
            System.err.println("[Client] Please ensure:");
            System.err.println("[Client]   1. The server is running");
            System.err.println("[Client]   2. The host and port are correct");
            System.err.println("[Client]   3. No firewall is blocking the connection");
            System.exit(1);
            
        } catch (IOException e) {
            System.err.println("[Client] ERROR: Communication error with server");
            System.err.println("[Client] Reason: " + e.getMessage());
            System.exit(1);
            
        } finally {
            if (userInput != null) {
                userInput.close();
            }
        }
    }
    
    /**
     * Prompts user for data and submits it to the server.
     * 
     * @return true if submission successful, false otherwise
     */
    private static boolean submitData() {
        try {
            System.out.println("╔════════════════════════════════════════════════════╗");
            System.out.println("║           SUBMIT CO2 READING                       ║");
            System.out.println("╚════════════════════════════════════════════════════╝");
            
            // Get User ID
            String userId = promptForUserId();
            if (userId == null) return false; // User cancelled
            
            // Get Postcode
            String postcode = promptForPostcode();
            if (postcode == null) return false; // User cancelled
            
            // Get CO2 Reading
            Double co2Reading = promptForCo2Reading();
            if (co2Reading == null) return false; // User cancelled
            
            // Send data to server
            String dataMessage = String.format("DATA|%s|%s|%.2f", userId, postcode, co2Reading);
            serverOutput.println(dataMessage);
            
            System.out.println("\n[Client] Sending data to server...");
            
            // Read server response
            readServerMessages();
            
            return true;
            
        } catch (Exception e) {
            System.err.println("[Client] ERROR during submission: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Prompts user for User ID with validation.
     * 
     * @return Valid user ID or null if cancelled
     */
    private static String promptForUserId() {
        while (true) {
            System.out.print("\nEnter User ID (e.g., st20308217) or 'cancel' to abort: ");
            String input = userInput.nextLine().trim();
            
            if (input.equalsIgnoreCase("cancel")) {
                return null;
            }
            
            if (Validator.isValidUserId(input)) {
                return input;
            } else {
                System.out.println("❌ " + Validator.getUserIdError(input));
                System.out.println("   Format: 2 letters followed by 8 digits");
            }
        }
    }
    
    /**
     * Prompts user for Postcode with validation.
     * 
     * @return Valid postcode or null if cancelled
     */
    private static String promptForPostcode() {
        while (true) {
            System.out.print("\nEnter Postcode (e.g., CF991SN) or 'cancel' to abort: ");
            String input = userInput.nextLine().trim();
            
            if (input.equalsIgnoreCase("cancel")) {
                return null;
            }
            
            if (Validator.isValidPostcode(input)) {
                return input.toUpperCase();
            } else {
                System.out.println("❌ " + Validator.getPostcodeError(input));
                System.out.println("   Examples: CF991SN, W1A 1AA, EC1A 1BB");
            }
        }
    }
    
    /**
     * Prompts user for CO2 reading with validation.
     * 
     * @return Valid CO2 reading or null if cancelled
     */
    private static Double promptForCo2Reading() {
        while (true) {
            System.out.print("\nEnter CO2 reading in ppm (e.g., 88.902) or 'cancel' to abort: ");
            String input = userInput.nextLine().trim();
            
            if (input.equalsIgnoreCase("cancel")) {
                return null;
            }
            
            try {
                double value = Validator.parseCo2Reading(input);
                
                if (Validator.isValidCo2Reading(value)) {
                    return value;
                } else {
                    System.out.println("❌ " + Validator.getCo2ReadingError(value));
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
    }
    
    /**
     * Reads and displays messages from the server.
     * Continues until a prompt or non-message is received.
     */
    private static void readServerMessages() {
        try {
            String message;
            while ((message = serverInput.readLine()) != null) {
                
                if (message.startsWith("WELCOME|")) {
                    String text = message.substring(8);
                    System.out.println("\n[Server] " + text);
                    
                } else if (message.startsWith("INFO|")) {
                    String text = message.substring(5);
                    System.out.println("[Server] " + text);
                    
                } else if (message.startsWith("SUCCESS|")) {
                    String text = message.substring(8);
                    System.out.println("✓ [Server] " + text);
                    
                } else if (message.startsWith("ERROR|")) {
                    String text = message.substring(6);
                    System.out.println("✗ [Server] " + text);
                    
                } else if (message.startsWith("PROMPT|")) {
                    // Server is ready for next input
                    break;
                    
                } else {
                    System.out.println("[Server] " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("[Client] ERROR reading from server: " + e.getMessage());
        }
    }
    
    /**
     * Closes all client resources.
     */
    private static void closeConnection() {
        try {
            if (serverOutput != null) {
                serverOutput.println("EXIT");
                serverOutput.close();
            }
            if (serverInput != null) {
                serverInput.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("[Client] Connection closed");
            
        } catch (IOException e) {
            System.err.println("[Client] ERROR closing connection: " + e.getMessage());
        }
    }
}