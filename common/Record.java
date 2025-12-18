package common;

import java.sql.Timestamp;
import java.util.Date;

public class Record {
    
    // Private final fields
    private final String userId;
    private final String postcode;
    private final double co2Reading;
    private final long timestamp;     // Unix timestamp in milliseconds
    
    public Record(String userId, String postcode, double co2Reading) {        
        this.userId = userId.trim();
        this.postcode = postcode.trim().toUpperCase();
        this.co2Reading = co2Reading;
        this.timestamp = new Date().getTime(); // Current system time in milliseconds
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getPostcode() {
        return postcode;
    }
    
    public double getCo2Reading() {
        return co2Reading;
    }
    
    public long getTimestampMillis() {
        return timestamp;
    }
    
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
    
    public String toCSVString() {
        return String.format("%s,%s,%s,%.2f", 
                            getTimestampString(), 
                            userId, 
                            postcode, 
                            co2Reading);
    }
    
    @Override
    public String toString() {
        return String.format(
            "Record{userId='%s', postcode='%s', co2Reading=%.2f ppm, timestamp='%s'}", 
            userId, postcode, co2Reading, getTimestampString()
        );
    }
}
