import { randomItem, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const CURRENCIES   = ['EUR', 'USD', 'GBP'];
const COUNTRIES    = ['FR', 'DE', 'GB', 'US', 'ES', 'IT'];
const MERCHANTS    = ['m_001', 'm_002', 'm_003', 'm_004', 'm_005'];
const MCCS         = ['5812', '5411', '4111', '5999', '7011'];
const CITIES       = ['Paris', 'London', 'Berlin', 'Madrid', 'Rome'];

let eventCounter = 0;

export function generatePaymentEvent(overrides = {}) {
    eventCounter++;
    const country = randomItem(COUNTRIES);
    return {
        eventId:    `k6-${Date.now()}-${eventCounter}`,
        eventType:  'PAYMENT_AUTHORIZED',
        occurredAt: new Date().toISOString(),
        transaction: {
            id:     `tx_k6_${eventCounter}`,
            amount: {
                value:    randomIntBetween(1, 500) + Math.random(),
                currency: randomItem(CURRENCIES),
            },
            cardReference: {
                hash:  `hash_${randomIntBetween(1, 100)}`,
                last4: `${randomIntBetween(1000, 9999)}`,
            },
            merchant: {
                id:      randomItem(MERCHANTS),
                mcc:     randomItem(MCCS),
                country: country,
            },
            ipGeo:  { country: country, city: randomItem(CITIES) },
            status: 'AUTHORIZED',
        },
        metadata: { correlationId: `k6-corr-${eventCounter}` },
        ...overrides,
    };
}
