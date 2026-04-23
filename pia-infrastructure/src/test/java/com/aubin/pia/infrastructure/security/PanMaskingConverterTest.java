package com.aubin.pia.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PanMaskingConverterTest {

    @Test
    void masks_luhn_valid_pan() {
        // Visa test card — valid Luhn
        String message = "Processing card 4111111111111111 for payment";
        String masked = mask(message);
        assertThat(masked).contains("****-****-****-1111");
        assertThat(masked).doesNotContain("4111111111111111");
    }

    @Test
    void does_not_mask_invalid_luhn() {
        String message = "Reference number 1234567890123456 processed";
        String masked = mask(message);
        assertThat(masked).contains("1234567890123456");
    }

    @Test
    void does_not_mask_short_numbers() {
        String message = "Amount: 120.50, txId: 123456789012";
        String masked = mask(message);
        assertThat(masked).isEqualTo(message);
    }

    @Test
    void masks_16_digit_mastercard() {
        // Mastercard test number — valid Luhn
        String message = "Card 5500005555555559 charged";
        String masked = mask(message);
        assertThat(masked).contains("****-****-****-5559");
    }

    private static String mask(String input) {
        return PanMaskingConverter.applyMask(input);
    }
}
