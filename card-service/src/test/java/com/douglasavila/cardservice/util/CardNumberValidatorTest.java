package com.douglasavila.cardservice.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CardNumberValidatorTest {

    @Test
    void isValidPan_returnsFalse_whenInputIsNull() {
        assertThat(CardNumberValidator.isValidPan(null)).isFalse();
    }

    @Test
    void isValidPan_acceptsValidLengthDigits() {
        assertThat(CardNumberValidator.isValidPan("4111111111111")).isTrue();        // 13
        assertThat(CardNumberValidator.isValidPan("4111111111111111")).isTrue();     // 16
        assertThat(CardNumberValidator.isValidPan("4111111111111111111")).isTrue();  // 19
    }

    @Test
    void isValidPan_rejectsTooShortOrTooLong() {
        assertThat(CardNumberValidator.isValidPan("411111111111")).isFalse();        // 12
        assertThat(CardNumberValidator.isValidPan("41111111111111111111")).isFalse();// 20
    }

    @Test
    void isValidPan_rejectsNonDigits() {
        assertThat(CardNumberValidator.isValidPan("4111a11111111111")).isFalse();
        assertThat(CardNumberValidator.isValidPan("4111_111111111111")).isFalse();
        assertThat(CardNumberValidator.isValidPan("4111.111111111111")).isFalse();
    }

    @Test
    void isValidPan_allowsSpacesAndDashes() {
        assertThat(CardNumberValidator.isValidPan("4111 1111 1111 1111")).isTrue();
        assertThat(CardNumberValidator.isValidPan("4111-1111-1111-1111")).isTrue();
        assertThat(CardNumberValidator.isValidPan("4111 - 1111 - 1111 - 1111")).isTrue();
    }

    @Test
    void isValidPan_rejectsAllSameDigit() {
        assertThat(CardNumberValidator.isValidPan("0000000000000")).isFalse();
        assertThat(CardNumberValidator.isValidPan("1111111111111111")).isFalse();
        assertThat(CardNumberValidator.isValidPan("9999 9999 9999 9999")).isFalse();
    }

    @Test
    void isValidPan_rejectsNormalizedEmptyOrInvalidAfterCleanup() {
        assertThat(CardNumberValidator.isValidPan("---- ---- ---- ----")).isFalse();
        assertThat(CardNumberValidator.isValidPan("            ")).isFalse();
    }

    @Test
    void isValidPan_doesNotApplyLuhnValidation() {
        // Intentionally non-Luhn-valid but structurally valid
        assertThat(CardNumberValidator.isValidPan("4111111111111123")).isTrue();
    }
}
