package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    // Configuration constants
    private static final int MAX_CLIENTS = 4;
    private static final String CSV_FILE_PATH = "data/records.csv";
    
    // Server state
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private CSVManager csvManager;
    private volatile boolean serverRunning = true;
    private int portNumber;
    
    public static void main(String[] args) {
        // Validate command line arguments
        if (args.length != 1) {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("Usage: java server.Server <port>");
            System.err.println("Example: java server.Server 8080");
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
            System.err.println("Example: java server.Server 8080");
            System.exit(1);
            return; // Unreachable but satisfies compiler
        }
        
        Server server = new Server(port);
        server.StartServer();
    }
    
    public Server(int port) {
        portNumber = port;
        csvManager = CSVManager.getInstance(CSV_FILE_PATH);
    }

    public void StartServer() {
        try {
            // Create server socket
            serverSocket = new ServerSocket(portNumber);
            System.out.println("[Server] Server started successfully");
            System.out.println("[Server] Listening on port: " + portNumber);
            System.out.println("[Server] Maximum concurrent clients: " + MAX_CLIENTS);
            
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
                    
                    // Create handler for this client
                    ClientHandler handler = new ClientHandler(clientSocket, csvManager);
                    
                    // Submit handler to thread pool
                    threadPool.execute(handler);
                    
                    System.out.println("[Server] New client accepted");
                    
                } catch (IOException e) {
                    if (serverRunning) {
                        System.err.println("[Server] ERROR accepting client connection: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("[Server] FATAL ERROR: Could not start server on port " + portNumber);
            System.err.println("[Server] Reason: " + e.getMessage());
            
            if (e.getMessage().contains("Address already in use")) {
                System.err.println("[Server] Port " + portNumber + " is already in use by another application");
                System.err.println("[Server] Try a different port number or stop the conflicting application");
            }
            
            System.exit(1);
        }
    }
    
    private void shutdownServer() {
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
        
        System.out.println("[Server] Server shutdown complete");
    }
}
