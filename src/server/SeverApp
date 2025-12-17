package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServerApp is the main server application for the CO2 Monitoring System.
 * Accepts client connections and delegates each to a separate thread.
 * Uses a fixed thread pool to handle up to 4 concurrent clients.
 * 
 * Usage: java server.ServerApp <port>
 * Example: java server.ServerApp 8080
 * 
 * @author CO2 Monitoring System Team
 * @version 1.0
 */
public class ServerApp {
    
    // Configuration constants
    private static final int MAX_CLIENTS = 4;
    private static final String CSV_FILE_PATH = "data/records.csv";
    
    // Server state
    private static ServerSocket serverSocket;
    private static ExecutorService threadPool;
    private static CSVManager csvManager;
    private static AtomicInteger clientCounter = new AtomicInteger(0);
    private static volatile boolean serverRunning = true;
    
    /**
     * Main entry point for the server application.
     * 
     * @param args Command line arguments: args[0] = port number
     */
    public static void main(String[] args) {
        // Validate command line arguments
        if (args.length != 1) {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("Usage: java server.ServerApp <port>");
            System.err.println("Example: java server.ServerApp 8080");
            System.exit(1);
        }
        
        // Parse port number
        int port;
        try {
            port = Integer.parseInt(args[0]);
            
            if (port < 1024 || port > 65535) {
                System.err.println("ERROR: Port number must be between 1024 and 65535");
                System.err.println("Ports below 1024 require administrative privileges");
                System.exit(1);
            }
            
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid port number. Must be an integer.");
            System.err.println("Example: java server.ServerApp 8080");
            System.exit(1);
            return; // Unreachable but satisfies compiler
        }
        
        // Start the server
        startServer(port);
    }
    
    /**
     * Initializes and starts the server.
     * 
     * @param port The port number to listen on
     */
    private static void startServer(int port) {
        try {
            // Initialize CSV Manager
            csvManager = CSVManager.getInstance(CSV_FILE_PATH);
            System.out.println("╔════════════════════════════════════════════════════╗");
            System.out.println("║   CO2 MONITORING SYSTEM - SERVER APPLICATION      ║");
            System.out.println("╚════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("[Server] CSV Database: " + csvManager.getFilePath());
            System.out.println("[Server] Current records in database: " + csvManager.getRecordCount());
            System.out.println();
            
            // Create server socket
            serverSocket = new ServerSocket(port);
            System.out.println("[Server] Server started successfully");
            System.out.println("[Server] Listening on port: " + port);
            System.out.println("[Server] Maximum concurrent clients: " + MAX_CLIENTS);
            System.out.println("[Server] Waiting for client connections...");
            System.out.println("[Server] Press Ctrl+C to stop the server");
            System.out.println("─────────────────────────────────────────────────────");
            
            // Create fixed thread pool (max 4 concurrent clients)
            threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
            
            // Setup shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[Server] Shutdown signal received");
                shutdownServer();
            }));
            
            // Main server loop - accept client connections
            while (serverRunning) {
                try {
                    // Accept incoming client connection (blocking call)
                    Socket clientSocket = serverSocket.accept();
                    
                    // Increment client counter and assign ID
                    int clientId = clientCounter.incrementAndGet();
                    
                    // Create handler for this client
                    ClientHandler handler = new ClientHandler(clientSocket, csvManager, clientId);
                    
                    // Submit handler to thread pool
                    threadPool.execute(handler);
                    
                    System.out.println("[Server] New client accepted. Assigned ID: " + clientId);
                    
                } catch (IOException e) {
                    if (serverRunning) {
                        System.err.println("[Server] ERROR accepting client connection: " + e.getMessage());
                    }
                    // If server is shutting down, this is expected
                }
            }
            
        } catch (IOException e) {
            System.err.println("[Server] FATAL ERROR: Could not start server on port " + port);
            System.err.println("[Server] Reason: " + e.getMessage());
            
            if (e.getMessage().contains("Address already in use")) {
                System.err.println("[Server] Port " + port + " is already in use by another application");
                System.err.println("[Server] Try a different port number or stop the conflicting application");
            }
            
            System.exit(1);
        }
    }
    
    /**
     * Shuts down the server gracefully.
     * Closes server socket and terminates thread pool.
     */
    private static void shutdownServer() {
        serverRunning = false;
        
        System.out.println("[Server] Shutting down server...");
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[Server] Server socket closed");
            }
        } catch (IOException e) {
            System.err.println("[Server] ERROR closing server socket: " + e.getMessage());
        }
        
        // Shutdown thread pool
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                // Wait for threads to finish (max 10 seconds)
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                    System.out.println("[Server] Thread pool forcefully shut down");
                } else {
                    System.out.println("[Server] All client threads terminated gracefully");
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Final statistics
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("[Server] Total clients served: " + clientCounter.get());
        System.out.println("[Server] Total records in database: " + csvManager.getRecordCount());
        System.out.println("[Server] Server shutdown complete");
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║              SERVER STOPPED                        ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }
}
