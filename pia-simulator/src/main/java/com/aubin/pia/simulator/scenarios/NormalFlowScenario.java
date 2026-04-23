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

/** Generates realistic low-value e-commerce transactions across EU merchants. */
@Component
public class NormalFlowScenario {

    private static final List<String[]> MERCHANTS =
            List.of(
                    new String[] {"m_amazon_fr", "5999", "FR"},
                    new String[] {"m_fnac_fr", "5734", "FR"},
                    new String[] {"m_zalando_de", "5651", "DE"},
                    new String[] {"m_booking_nl", "4722", "NL"},
                    new String[] {"m_spotify_se", "5815", "SE"});

    private static final List<String[]> CARDS =
            List.of(
                    new String[] {"card_001", "4242"},
                    new String[] {"card_002", "1234"},
                    new String[] {"card_003", "9876"});

    private final Random random = new Random();

    public PaymentEventDto generate() {
        String[] card = CARDS.get(random.nextInt(CARDS.size()));
        String[] merchant = MERCHANTS.get(random.nextInt(MERCHANTS.size()));
        BigDecimal amount =
                BigDecimal.valueOf(5 + random.nextInt(296))
                        .add(BigDecimal.valueOf(random.nextInt(100), 2));

        return buildEvent(card[0], card[1], merchant, amount, "AUTHORIZED");
    }

    private PaymentEventDto buildEvent(
            String cardSeed, String last4, String[] merchant, BigDecimal amount, String status) {
        String txId = "tx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String cardHash = sha256(cardSeed);
        String corrId = UUID.randomUUID().toString();

        return new PaymentEventDto(
                UUID.randomUUID().toString(),
                "PAYMENT_AUTHORIZED",
                Instant.now(),
                new PaymentEventDto.TransactionDto(
                        txId,
                        new PaymentEventDto.AmountDto(amount, "EUR"),
                        new PaymentEventDto.CardReferenceDto(cardHash, last4),
                        new PaymentEventDto.MerchantDto(merchant[0], merchant[1], merchant[2]),
                        new PaymentEventDto.IpGeoDto(merchant[2], "Paris"),
                        status),
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
