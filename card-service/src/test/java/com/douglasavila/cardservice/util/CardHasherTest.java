package com.douglasavila.cardservice.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CardHasherTest {

    @Test
    void hash_returnsDeterministicSha256Hex_andChangesWithSalt() {
        String pan = "4111111111111111";

        CardHasher hasherA1 = new CardHasher("salt-A");
        CardHasher hasherA2 = new CardHasher("salt-A");
        CardHasher hasherB = new CardHasher("salt-B");

        String h1 = hasherA1.hash(pan);
        String h2 = hasherA2.hash(pan);
        String h3 = hasherB.hash(pan);

        assertThat(h1).isEqualTo(h2);
        assertThat(h1).isNotEqualTo(h3);
        assertThat(h1).matches("^[0-9a-f]{64}$");
        assertThat(h3).matches("^[0-9a-f]{64}$");
    }

    @Test
    void hash_trimsCardNumberBeforeHashing() {
        CardHasher hasher = new CardHasher("salt");

        String h1 = hasher.hash("4111111111111111");
        String h2 = hasher.hash("  4111111111111111  ");

        assertThat(h2).isEqualTo(h1);
    }

    @Test
    void last4_returnsLast4_whenLengthAtLeast4() {
        CardHasher hasher = new CardHasher("salt");

        assertThat(hasher.last4("4111111111111111")).isEqualTo("1111");
        assertThat(hasher.last4("1234")).isEqualTo("1234");
        assertThat(hasher.last4("00001234")).isEqualTo("1234");
    }

    @Test
    void last4_returnsInput_whenNullOrShorterThan4() {
        CardHasher hasher = new CardHasher("salt");

        assertThat(hasher.last4(null)).isNull();
        assertThat(hasher.last4("")).isEqualTo("");
        assertThat(hasher.last4("1")).isEqualTo("1");
        assertThat(hasher.last4("12")).isEqualTo("12");
        assertThat(hasher.last4("123")).isEqualTo("123");
    }
}
