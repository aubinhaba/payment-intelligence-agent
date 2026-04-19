package com.aubin.pia.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CardReferenceTest {

    @Test
    void should_create_valid_card_reference() {
        CardReference ref = new CardReference("abc123hash", "4242");
        assertThat(ref.hash()).isEqualTo("abc123hash");
        assertThat(ref.last4()).isEqualTo("4242");
    }

    @Test
    void should_reject_blank_hash() {
        assertThatThrownBy(() -> new CardReference("  ", "4242"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hash");
    }

    @Test
    void should_reject_null_hash() {
        assertThatThrownBy(() -> new CardReference(null, "4242"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_reject_last4_with_letters() {
        assertThatThrownBy(() -> new CardReference("hash", "42AB"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4 digits");
    }

    @Test
    void should_reject_last4_too_short() {
        assertThatThrownBy(() -> new CardReference("hash", "424"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4 digits");
    }

    @Test
    void should_reject_last4_too_long() {
        assertThatThrownBy(() -> new CardReference("hash", "42421"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4 digits");
    }

    @Test
    void should_reject_null_last4() {
        assertThatThrownBy(() -> new CardReference("hash", null))
                .isInstanceOf(NullPointerException.class);
    }
}
