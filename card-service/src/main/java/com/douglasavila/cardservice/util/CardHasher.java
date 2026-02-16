package com.douglasavila.cardservice.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CardHasher {

    private final String salt;

    public CardHasher(String salt) {
        this.salt = salt;
    }

    public String hash(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = cardNumber.trim() + salt;
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public String last4(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
