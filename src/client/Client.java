
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ConnectException;

public class Client {
    private Socket socket;
    private String hostname;
    private int port;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    
    public void startClient() {    
        try {
            // Connect to server
            System.out.println("[Client] Connecting to server...");
            
            //Attempt to connect to server
            this.socket = new Socket(this.hostname, this.port);
            
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            new ReadThread(this.socket, in).start();
            new WriteThread(this.socket, out, in).start();
        } catch (UnknownHostException e) {
            System.err.println("[Client] ERROR: Unknown host '" + this.hostname + "'");
            System.err.println("[Client] Please check the hostname or IP address");
            System.exit(1);
        } catch (ConnectException e) {
            System.err.println("[Client] ERROR: Cannot connect to server");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("[Client] ERROR: Communication error with server");
            System.err.println("[Client] Reason: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        // Validate command line arguments
        if (args.length < 2) {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("Usage: java client.ClientApp <host> <port>");
            System.err.println("Example: java client.ClientApp localhost 8080");
            System.exit(1);
        }
        
        String host = args[0];
        int port;
        
        // Parse port number (Must be in range 1024-65535)
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
            return;
        }
    
        Client ClientApp = new Client(host, port);
        ClientApp.startClient();
    }
}
