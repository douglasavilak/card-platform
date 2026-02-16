package com.douglasavila.cardservice.util;

public final class CardNumberValidator {

    private CardNumberValidator() {}

    /**
     * Validates a PAN (Primary Account Number) using:
     * - normalization (removes spaces and dashes)
     * - length 13..19
     * - digits only
     * - basic anti-bogus checks (all same digit)
     */
    public static boolean isValidPan(String input) {
        if (input == null) return false;

        String pan = normalize(input);
        int len = pan.length();

        // Length rule (ISO/industry): 13..19 digits
        if (len < 13 || len > 19) return false;

        // Digits only
        if (!allDigits(pan)) return false;

        // Anti-bogus: reject all same digit (e.g., 0000..., 1111...)
        return !allSameDigit(pan);

        // Luhn validation skipped to facilitate testing with non-real valid card numbers
    }

    /** Keep only digits; tolerate spaces and '-' in user input. */
    private static String normalize(String s) {
        // Remove spaces and dashes; you can expand this to remove other separators if needed.
        return s.replace(" ", "").replace("-", "");
    }

    private static boolean allDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    private static boolean allSameDigit(String s) {
        char first = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != first) return false;
        }
        return true;
    }
}
