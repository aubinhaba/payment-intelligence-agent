package com.aubin.pia.simulator.scenarios;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.aubin.pia.simulator.dto.PaymentEventDto;

/**
 * Simulates a fraud burst: one card performing many high-value transactions across different
 * countries in a short time window.
 */
@Component
public class FraudBurstScenario {

    private static final List<String[]> HIGH_RISK_MERCHANTS =
            List.of(
                    new String[] {"m_luxury_ae", "5944", "AE"},
                    new String[] {"m_casino_mt", "7995", "MT"},
                    new String[] {"m_jewelry_sg", "5094", "SG"},
                    new String[] {"m_electronics_hk", "5732", "HK"});

    private final Random random = new Random();

    public PaymentEventDto generate() {
        String[] merchant = HIGH_RISK_MERCHANTS.get(random.nextInt(HIGH_RISK_MERCHANTS.size()));
        BigDecimal amount =
                BigDecimal.valueOf(1000 + random.nextInt(9000))
                        .add(BigDecimal.valueOf(random.nextInt(100), 2));

        String cardHash = sha256("fraud_card_burst");
        String txId = "tx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String corrId = UUID.randomUUID().toString();

        return new PaymentEventDto(
                UUID.randomUUID().toString(),
                "PAYMENT_AUTHORIZED",
                Instant.now(),
                new PaymentEventDto.TransactionDto(
                        txId,
                        new PaymentEventDto.AmountDto(amount, "EUR"),
                        new PaymentEventDto.CardReferenceDto(cardHash, "9999"),
                        new PaymentEventDto.MerchantDto(merchant[0], merchant[1], merchant[2]),
                        new PaymentEventDto.IpGeoDto(merchant[2], "Unknown"),
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
