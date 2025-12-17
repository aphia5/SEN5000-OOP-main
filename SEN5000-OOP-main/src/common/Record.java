package common;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Record class represents a single CO2 reading submission.
 * Immutable data object that stores user ID, postcode, CO2 reading, and timestamp.
 * 
 * @author CO2 Monitoring System Team
 * @version 1.0
 */
public class Record {
    
    // Private final fields - immutable once created
    private final String userId;
    private final String postcode;
    private final double co2Reading;  // Using primitive for better performance
    private final long timestamp;     // Unix timestamp in milliseconds
    
    /**
     * Constructor creates a Record with current timestamp.
     * 
     * @param userId The unique identifier for the researcher
     * @param postcode The location where reading was taken
     * @param co2Reading The CO2 concentration in parts per million (ppm)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Record(String userId, String postcode, double co2Reading) {
        // Validate inputs before creating object
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (postcode == null || postcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Postcode cannot be null or empty");
        }
        if (co2Reading < 0) {
            throw new IllegalArgumentException("CO2 reading cannot be negative");
        }
        
        this.userId = userId.trim();
        this.postcode = postcode.trim().toUpperCase(); // Standardize postcode format
        this.co2Reading = co2Reading;
        this.timestamp = new Date().getTime(); // Current system time in milliseconds
    }
    
    /**
     * Gets the user ID.
     * @return The user ID string
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Gets the postcode.
     * @return The postcode string
     */
    public String getPostcode() {
        return postcode;
    }
    
    /**
     * Gets the CO2 reading.
     * @return The CO2 concentration in ppm
     */
    public double getCo2Reading() {
        return co2Reading;
    }
    
    /**
     * Gets the timestamp as Unix time in milliseconds.
     * @return The timestamp value
     */
    public long getTimestampMillis() {
        return timestamp;
    }
    
    /**
     * Gets the timestamp as a formatted string.
     * Format: YYYY-MM-DD HH:MM:SS
     * 
     * @return Formatted timestamp string
     */
    public String getTimestampString() {
        Timestamp ts = new Timestamp(timestamp);
        String fullString = ts.toString(); // Format: "YYYY-MM-DD HH:MM:SS.fff"
        
        // Remove milliseconds (everything after the decimal point)
        int decimalIndex = fullString.lastIndexOf('.');
        if (decimalIndex > 0) {
            return fullString.substring(0, decimalIndex);
        }
        return fullString;
    }
    
    /**
     * Converts this record to CSV format.
     * Format: timestamp,user id,postcode,co2 ppm
     * 
     * @return CSV-formatted string representation
     */
    public String toCSVString() {
        return String.format("%s,%s,%s,%.2f", 
                            getTimestampString(), 
                            userId, 
                            postcode, 
                            co2Reading);
    }
    
    /**
     * Returns a human-readable string representation of the record.
     * Used for debugging and console output.
     * 
     * @return Formatted string with all record details
     */
    @Override
    public String toString() {
        return String.format(
            "Record{userId='%s', postcode='%s', co2Reading=%.2f ppm, timestamp='%s'}", 
            userId, postcode, co2Reading, getTimestampString()
        );
    }
    
    /**
     * Displays the record data in a formatted console output.
     * Used for debugging purposes.
     */
    public void displayData() {
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│        CO2 READING RECORD          │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.printf("│ User ID    : %-20s │%n", userId);
        System.out.printf("│ Postcode   : %-20s │%n", postcode);
        System.out.printf("│ CO2 Level  : %-17.2f ppm │%n", co2Reading);
        System.out.printf("│ Timestamp  : %-20s │%n", getTimestampString());
        System.out.println("└─────────────────────────────────────┘");
    }
}