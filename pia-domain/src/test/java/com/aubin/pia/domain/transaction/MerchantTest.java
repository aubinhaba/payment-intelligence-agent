package com.aubin.pia.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MerchantTest {

    @Test
    void should_create_valid_merchant() {
        Merchant merchant = new Merchant("m_001", "5812", "FR");
        assertThat(merchant.id()).isEqualTo("m_001");
        assertThat(merchant.mcc()).isEqualTo("5812");
        assertThat(merchant.country()).isEqualTo("FR");
    }

    @Test
    void should_reject_blank_id() {
        assertThatThrownBy(() -> new Merchant("", "5812", "FR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void should_reject_non_4_digit_mcc() {
        assertThatThrownBy(() -> new Merchant("m_001", "581", "FR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MCC");
    }

    @Test
    void should_reject_mcc_with_letters() {
        assertThatThrownBy(() -> new Merchant("m_001", "58AB", "FR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MCC");
    }

    @Test
    void should_reject_non_alpha2_country() {
        assertThatThrownBy(() -> new Merchant("m_001", "5812", "FRA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alpha-2");
    }

    @Test
    void should_reject_null_fields() {
        assertThatThrownBy(() -> new Merchant(null, "5812", "FR"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Merchant("m_001", null, "FR"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Merchant("m_001", "5812", null))
                .isInstanceOf(NullPointerException.class);
    }
}
