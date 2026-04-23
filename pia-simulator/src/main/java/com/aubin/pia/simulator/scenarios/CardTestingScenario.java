package com.aubin.pia.simulator.scenarios;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.aubin.pia.simulator.dto.PaymentEventDto;

/**
 * Simulates card-testing behaviour: many micro-transactions (< 2 EUR) sent from the same card to
 * the same merchant in quick succession to verify a stolen card number is valid.
 */
@Component
public class CardTestingScenario {

    private static final String MERCHANT_ID = "m_gas_station_fr";
    private static final String MERCHANT_MCC = "5541";
    private static final String MERCHANT_COUNTRY = "FR";

    public PaymentEventDto generate() {
        BigDecimal microAmount =
                BigDecimal.valueOf(0.50 + Math.random() * 1.49)
                        .setScale(2, java.math.RoundingMode.HALF_UP);
        String cardHash = sha256("card_testing_victim");
        String txId = "tx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String corrId = UUID.randomUUID().toString();

        return new PaymentEventDto(
                UUID.randomUUID().toString(),
                "PAYMENT_AUTHORIZED",
                Instant.now(),
                new PaymentEventDto.TransactionDto(
                        txId,
                        new PaymentEventDto.AmountDto(microAmount, "EUR"),
                        new PaymentEventDto.CardReferenceDto(cardHash, "0001"),
                        new PaymentEventDto.MerchantDto(
                                MERCHANT_ID, MERCHANT_MCC, MERCHANT_COUNTRY),
                        new PaymentEventDto.IpGeoDto(MERCHANT_COUNTRY, "Lyon"),
                        "AUTHORIZED"),
                new PaymentEventDto.MetadataDto(corrId));
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
