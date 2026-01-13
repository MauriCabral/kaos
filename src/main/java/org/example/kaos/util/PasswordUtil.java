package org.example.kaos.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility class for password hashing and verification.
 */
public class PasswordUtil {

    private static final int COST = 12; // Work factor for BCrypt

    /**
     * Hashes a plain text password.
     *
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(COST, plainPassword.toCharArray());
    }

    /**
     * Verifies a plain text password against a hashed password.
     *
     * @param plainPassword the plain text password
     * @param hashedPassword the hashed password
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}