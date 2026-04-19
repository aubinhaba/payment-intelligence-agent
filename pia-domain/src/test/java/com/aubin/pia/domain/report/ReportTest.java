package com.aubin.pia.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.aubin.pia.domain.transaction.TransactionId;

class ReportTest {

    @Test
    void should_generate_report_with_correct_fields() {
        ReportContent content =
                new ReportContent("High-risk transaction", "## Analysis\nDetails...");
        Report report =
                Report.generate(new ReportId("r_001"), new TransactionId("tx_001"), content);

        assertThat(report.id().value()).isEqualTo("r_001");
        assertThat(report.transactionId().value()).isEqualTo("tx_001");
        assertThat(report.content().summary()).isEqualTo("High-risk transaction");
        assertThat(report.generatedAt()).isNotNull();
    }

    @Test
    void should_reject_blank_summary() {
        assertThatThrownBy(() -> new ReportContent("  ", "body"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("summary");
    }

    @Test
    void should_reject_null_content() {
        assertThatThrownBy(
                        () ->
                                Report.generate(
                                        new ReportId("r_001"), new TransactionId("tx_001"), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_accept_empty_markdown_body() {
        ReportContent content = new ReportContent("Summary", "");
        assertThat(content.markdownBody()).isEmpty();
    }
}
