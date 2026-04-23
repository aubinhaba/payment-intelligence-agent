package com.aubin.pia.infrastructure.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback converter that detects digit sequences of length 13-19 passing the Luhn check and
 * replaces them with {@code ****-****-****-XXXX} (last-4 preserved for diagnostics).
 *
 * <p>Register in logback-spring.xml: {@code <conversionRule conversionWord="maskedMsg"
 * converterClass="...PanMaskingConverter"/>}
 */
public class PanMaskingConverter extends MessageConverter {

    private static final Pattern CANDIDATE = Pattern.compile("\\b(\\d{13,19})\\b");

    @Override
    public String convert(ILoggingEvent event) {
        return applyMask(super.convert(event));
    }

    /** Applies PAN masking to an arbitrary string. Package-visible for testing. */
    static String applyMask(String message) {
        Matcher matcher = CANDIDATE.matcher(message);
        if (!matcher.find()) {
            return message;
        }
        StringBuffer sb = new StringBuffer();
        matcher.reset();
        while (matcher.find()) {
            String digits = matcher.group(1);
            String replacement =
                    isLuhnValid(digits)
                            ? "****-****-****-" + digits.substring(digits.length() - 4)
                            : digits;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    static boolean isLuhnValid(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
