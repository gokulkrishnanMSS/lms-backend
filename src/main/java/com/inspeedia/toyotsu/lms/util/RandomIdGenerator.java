package com.inspeedia.toyotsu.lms.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RandomIdGenerator {

    // Define a SecureRandom instance for generating random values
    private final SecureRandom secureRandom = new SecureRandom();

    // Method to generate a random string for an object name
    public String generateSecureRandomString() {
        // Define the length of the random part of the string
        // You can adjust this length
        byte[] randomBytes = new byte[32];  // Create a byte array of the specified length
        secureRandom.nextBytes(randomBytes);  // Fill the byte array with random data

        // Encode the random bytes into a URL-safe Base64 string and format it to match your example
        return formatBase64String(randomBytes);
    }

    // Helper method to format the random string into a specific pattern
    private String formatBase64String(byte[] bytes) {
        // Base64 encoding, URL-safe (no '+' and '/')
        String encodedString = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Example formatting pattern similar to your required format
        // This assumes your format needs to be split into parts with hyphens
        return encodedString.substring(0, 8) + "-" + encodedString.substring(8, 16)
                + "-" + encodedString.substring(16, 24) + "-" + encodedString.substring(24);
    }
}
