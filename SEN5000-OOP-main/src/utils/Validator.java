package utils;

import java.util.regex.Pattern;

/**
 * Validator utility class provides static methods for input validation.
 * Ensures data integrity before processing CO2 readings.
 * 
 * @author CO2 Monitoring System Team
 * @version 1.0
 */
public class Validator {
    
    // Regular expression patterns for validation
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z]{2}\\d{8}$");
    // UK postcode format: 1-2 letters, 1-2 digits, optional digit/letter, space, digit, 2 letters
    private static final Pattern POSTCODE_PATTERN = Pattern.compile(
        "^[A-Z]{1,2}\\d{1,2}[A-Z]?\\s?\\d[A-Z]{2}$"
    );
    
    // CO2 reading constraints
    private static final double MIN_CO2 = 0.0;
    private static final double MAX_CO2 = 10000.0; // Reasonable upper limit for CO2 ppm
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Validator() {
        throw new AssertionError("Validator class cannot be instantiated");
    }
    
    /**
     * Validates a user ID format.
     * Expected format: 2 letters followed by 8 digits (e.g., "st20308217")
     * 
     * @param userId The user ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return USER_ID_PATTERN.matcher(userId.trim()).matches();
    }
    
    /**
     * Validates a UK postcode format.
     * Accepts various UK postcode formats with or without space.
     * Examples: "CF991SN", "CF99 1SN", "W1A 1AA", "EC1A1BB"
     * 
     * @param postcode The postcode to validate
     * @return true if valid UK postcode format, false otherwise
     */
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
    
    /**
     * Validates a CO2 reading value.
     * Must be a positive number within reasonable atmospheric range.
     * 
     * @param co2Reading The CO2 concentration in ppm
     * @return true if valid, false otherwise
     */
    public static boolean isValidCo2Reading(double co2Reading) {
        // Check if the value is a valid number (not NaN or Infinite)
        if (Double.isNaN(co2Reading) || Double.isInfinite(co2Reading)) {
            return false;
        }
        
        // Check if within acceptable range
        return co2Reading >= MIN_CO2 && co2Reading <= MAX_CO2;
    }
    
    /**
     * Validates all record fields at once.
     * Combines all validation checks for convenience.
     * 
     * @param userId The user ID to validate
     * @param postcode The postcode to validate
     * @param co2Reading The CO2 reading to validate
     * @return true if all fields are valid, false otherwise
     */
    public static boolean validateRecord(String userId, String postcode, double co2Reading) {
        return isValidUserId(userId) && 
               isValidPostcode(postcode) && 
               isValidCo2Reading(co2Reading);
    }
    
    /**
     * Gets a detailed error message for invalid user ID.
     * 
     * @param userId The user ID that failed validation
     * @return Descriptive error message
     */
    public static String getUserIdError(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "User ID cannot be empty";
        }
        return "Invalid User ID format. Expected: 2 letters + 8 digits (e.g., st20308217)";
    }
    
    /**
     * Gets a detailed error message for invalid postcode.
     * 
     * @param postcode The postcode that failed validation
     * @return Descriptive error message
     */
    public static String getPostcodeError(String postcode) {
        if (postcode == null || postcode.trim().isEmpty()) {
            return "Postcode cannot be empty";
        }
        return "Invalid UK postcode format. Examples: CF991SN, W1A 1AA, EC1A 1BB";
    }
    
    /**
     * Gets a detailed error message for invalid CO2 reading.
     * 
     * @param co2Reading The CO2 reading that failed validation
     * @return Descriptive error message
     */
    public static String getCo2ReadingError(double co2Reading) {
        if (Double.isNaN(co2Reading) || Double.isInfinite(co2Reading)) {
            return "CO2 reading must be a valid number";
        }
        if (co2Reading < MIN_CO2) {
            return "CO2 reading cannot be negative";
        }
        if (co2Reading > MAX_CO2) {
            return String.format("CO2 reading too high. Maximum: %.2f ppm", MAX_CO2);
        }
        return "Invalid CO2 reading";
    }
    
    /**
     * Attempts to parse a string as a double for CO2 reading.
     * Provides better error messages than standard parsing.
     * 
     * @param input The string to parse
     * @return The parsed double value
     * @throws NumberFormatException if parsing fails
     */
    public static double parseCo2Reading(String input) throws NumberFormatException {
        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException("CO2 reading cannot be empty");
        }
        
        try {
            return Double.parseDouble(input.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("CO2 reading must be a valid number (e.g., 88.902)");
        }
    }
}