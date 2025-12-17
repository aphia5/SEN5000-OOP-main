package server;

import common.Record;
import java.io.*;

/**
 * CSVManager handles thread-safe writing to the CSV database file.
 * Implements singleton pattern to ensure single point of file access.
 * Uses synchronized methods to prevent data corruption from concurrent writes.
 * 
 * @author CO2 Monitoring System Team
 * @version 1.0
 */
public class CSVManager {
    
    // Singleton instance
    private static CSVManager instance = null;
    
    // CSV file configuration
    private final String csvFilePath;
    private final String CSV_HEADER = "timestamp,user id,postcode,co2 ppm";
    
    /**
     * Private constructor for singleton pattern.
     * 
     * @param csvFilePath The path to the CSV file
     */
    private CSVManager(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        initializeCSVFile();
    }
    
    /**
     * Gets the singleton instance of CSVManager.
     * Creates instance if it doesn't exist.
     * 
     * @param csvFilePath The path to the CSV file
     * @return The singleton CSVManager instance
     */
    public static synchronized CSVManager getInstance(String csvFilePath) {
        if (instance == null) {
            instance = new CSVManager(csvFilePath);
        }
        return instance;
    }
    
    /**
     * Initializes the CSV file if it doesn't exist.
     * Creates parent directories and writes header row.
     */
    private void initializeCSVFile() {
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
    
    /**
     * Writes the CSV header row to the file.
     * Only called during initialization if file doesn't exist.
     * 
     * @throws IOException if writing fails
     */
    private void writeHeader() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, false))) {
            writer.write(CSV_HEADER);
            writer.newLine();
            writer.flush();
        }
    }
    
    /**
     * Writes a record to the CSV file in a thread-safe manner.
     * CRITICAL: synchronized keyword ensures only one thread can write at a time.
     * This prevents data corruption when multiple clients submit simultaneously.
     * 
     * @param record The Record object to write
     * @return true if write successful, false otherwise
     */
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
    
    /**
     * Reads all records from the CSV file.
     * Useful for debugging and verification.
     * 
     * @return String containing all file contents
     */
    public synchronized String readAllRecords() {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("[CSVManager] ERROR: Failed to read records: " + e.getMessage());
            return null;
        }
        
        return content.toString();
    }
    
    /**
     * Gets the total number of records in the CSV file (excluding header).
     * 
     * @return Number of data records
     */
    public synchronized int getRecordCount() {
        int count = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            boolean firstLine = true;
            
            while ((reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }
                count++;
            }
        } catch (IOException e) {
            System.err.println("[CSVManager] ERROR: Failed to count records: " + e.getMessage());
            return -1;
        }
        
        return count;
    }
    
    /**
     * Verifies that the CSV file exists and is accessible.
     * 
     * @return true if file exists and is readable/writable
     */
    public boolean isFileAccessible() {
        File file = new File(csvFilePath);
        return file.exists() && file.canRead() && file.canWrite();
    }
    
    /**
     * Gets the absolute path of the CSV file.
     * 
     * @return Absolute file path
     */
    public String getFilePath() {
        return new File(csvFilePath).getAbsolutePath();
    }
}