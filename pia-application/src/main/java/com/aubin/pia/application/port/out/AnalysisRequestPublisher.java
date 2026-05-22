package com.aubin.pia.application.port.out;

import com.aubin.pia.domain.transaction.TransactionId;

/**
 * Driven port for requesting an asynchronous agent analysis on a transaction.
 *
 * <p>Implementations publish the request to a downstream queue (e.g. SQS) so the analysis pipeline
 * is decoupled from the ingestion path. The receiving consumer invokes {@link
 * com.aubin.pia.application.usecase.AnalyzeAndReportUseCase}.
 */
public interface AnalysisRequestPublisher {
    void publish(TransactionId transactionId);
}
