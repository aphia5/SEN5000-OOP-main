package utils;

import java.util.regex.Pattern;

public class Validator {
    
    // Regular expression patterns for validation
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z]{2}\\d{8}$");

    // UK postcode format: 1-2 letters, 1-2 digits, optional digit/letter, space, digit, 2 letters
    private static final Pattern POSTCODE_PATTERN = Pattern.compile(
        "^[A-Z]{1,2}\\d{1,2}[A-Z]?\\s?\\d[A-Z]{2}$"
    );
    
    // CO2 reading constraints
    private static final double MIN_CO2 = 0.0;
    private static final double MAX_CO2 = 10000.0; // Reasonable upper limit
    
    private Validator() {
        throw new AssertionError("Validator class cannot be instantiated");
    }
    
    public static boolean isValidUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return USER_ID_PATTERN.matcher(userId.trim()).matches();
    }

    public static boolean isValidPostcode(String postcode) {
        if (postcode == null || postcode.trim().isEmpty()) {
            return false;
        }
        
        // Normalize: remove spaces and convert to uppercase for validation
        String normalized = postcode.trim().replaceAll("\\s+", "").toUpperCase();
        
        // Re-insert space for pattern matching: before last 3 characters
        if (normalized.length() >= 5 && normalized.length() <= 7) {
            int spacePosition = normalized.length() - 3;
            String formatted = normalized.substring(0, spacePosition) + " " + 
                             normalized.substring(spacePosition);
            return POSTCODE_PATTERN.matcher(formatted).matches();
        }
        
        return false;
    }
    
    public static boolean isValidCo2Reading(double co2Reading) {
        // Check if the value is a valid number (not NaN or Infinite)
        if (Double.isNaN(co2Reading) || Double.isInfinite(co2Reading)) {
            return false;
        }
        
        // Check if within acceptable range
        return co2Reading >= MIN_CO2 && co2Reading <= MAX_CO2;
    }
    
    public static boolean validateRecord(String userId, String postcode, double co2Reading) {
        return isValidUserId(userId) && 
               isValidPostcode(postcode) && 
               isValidCo2Reading(co2Reading);
    }
}