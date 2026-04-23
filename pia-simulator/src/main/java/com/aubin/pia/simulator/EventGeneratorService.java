package com.aubin.pia.simulator;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.aubin.pia.simulator.dto.PaymentEventDto;
import com.aubin.pia.simulator.scenarios.CardTestingScenario;
import com.aubin.pia.simulator.scenarios.FraudBurstScenario;
import com.aubin.pia.simulator.scenarios.NormalFlowScenario;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@Service
public class EventGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(EventGeneratorService.class);

    private final SqsTemplate sqsTemplate;
    private final NormalFlowScenario normalFlow;
    private final FraudBurstScenario fraudBurst;
    private final CardTestingScenario cardTesting;
    private final String queueName;
    private final Random random = new Random();

    public EventGeneratorService(
            SqsTemplate sqsTemplate,
            NormalFlowScenario normalFlow,
            FraudBurstScenario fraudBurst,
            CardTestingScenario cardTesting,
            @Value("${pia.simulator.queue-name:payment-events-queue}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.normalFlow = normalFlow;
        this.fraudBurst = fraudBurst;
        this.cardTesting = cardTesting;
        this.queueName = queueName;
    }

    @Scheduled(fixedDelayString = "${pia.simulator.interval-ms:1000}")
    public void generateEvent() {
        PaymentEventDto event = pickScenario();
        sqsTemplate.send(queueName, event);
        log.debug("simulator.sent scenario={} eventId={}", event.eventType(), event.eventId());
    }

    private PaymentEventDto pickScenario() {
        int roll = random.nextInt(100);
        if (roll < 5) {
            return fraudBurst.generate();
        } else if (roll < 10) {
            return cardTesting.generate();
        } else {
            return normalFlow.generate();
        }
    }
}
