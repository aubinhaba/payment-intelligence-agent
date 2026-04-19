package com.aubin.pia.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.Test;

class AmountTest {

    @Test
    void should_create_valid_amount() {
        Amount amount = Amount.of(new BigDecimal("100.00"), "EUR");
        assertThat(amount.value()).isEqualByComparingTo("100.00");
        assertThat(amount.currency()).isEqualTo(Currency.getInstance("EUR"));
    }

    @Test
    void should_accept_zero_amount() {
        Amount amount = Amount.of(BigDecimal.ZERO, "USD");
        assertThat(amount.value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_reject_negative_amount() {
        assertThatThrownBy(() -> Amount.of(new BigDecimal("-0.01"), "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void should_reject_null_value() {
        assertThatThrownBy(() -> new Amount(null, Currency.getInstance("EUR")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_reject_null_currency() {
        assertThatThrownBy(() -> new Amount(BigDecimal.ONE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_reject_invalid_currency_code() {
        assertThatThrownBy(() -> Amount.of(BigDecimal.ONE, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
