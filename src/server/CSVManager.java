package server;

import common.Record;
import java.io.*;

public class CSVManager {
    
    // Singleton instance
    private static CSVManager instance = null;
    
    // CSV file configuration
    private final String csvFilePath;
    private final String CSV_HEADER = "timestamp,user id,postcode,co2 ppm";
    
    private CSVManager(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        initialiseCSVFile();
    }
    
    public static synchronized CSVManager getInstance(String csvFilePath) {
        if (instance == null) {
            instance = new CSVManager(csvFilePath);
        }
        return instance;
    }
    
    private void initialiseCSVFile() {
        File file = new File(csvFilePath);
        
        try {
            // Create parent directories if they don't exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("[CSVManager] Created directory: " + parentDir.getAbsolutePath());
                }
            }
            
            // Create file with header if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
                writeHeader();
                System.out.println("[CSVManager] Initialized CSV file: " + csvFilePath);
            } else {
                System.out.println("[CSVManager] Using existing CSV file: " + csvFilePath);
            }
            
        } catch (IOException e) {
            System.err.println("[CSVManager] ERROR: Failed to initialize CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void writeHeader() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, false))) {
            writer.write(CSV_HEADER);
            writer.newLine();
            writer.flush();
        }
    }
    
    public synchronized boolean writeRecord(Record record) {
        if (record == null) {
            System.err.println("[CSVManager] ERROR: Cannot write null record");
            return false;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, true))) {
            // Append mode: true
            writer.write(record.toCSVString());
            writer.newLine();
            writer.flush(); // Ensure data is written to disk immediately
            
            System.out.println("[CSVManager] Record written successfully: " + record.getUserId());
            return true;
            
        } catch (IOException e) {
            System.err.println("[CSVManager] ERROR: Failed to write record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
