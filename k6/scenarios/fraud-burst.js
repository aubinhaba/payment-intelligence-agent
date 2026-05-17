import { sleep } from 'k6';
import { check } from 'k6';
import http from 'k6/http';

// Fraud burst: spike to 500 rps for 30 seconds to validate auto-scaling and DLQ behaviour.
// Run AFTER normal-flow to have baseline data in the system.
export const options = {
    scenarios: {
        fraud_burst: {
            executor: 'ramping-arrival-rate',
            startRate: 50,
            timeUnit: '1s',
            preAllocatedVUs: 100,
            maxVUs: 200,
            stages: [
                { target: 50,  duration: '15s' }, // baseline
                { target: 500, duration: '15s' }, // spike
                { target: 500, duration: '30s' }, // hold — triggers anomaly detection
                { target: 50,  duration: '15s' }, // return to baseline
                { target: 0,   duration: '15s' }, // cool down
            ],
        },
    },
    thresholds: {
        // During burst, we accept degraded latency but not errors
        http_req_failed:   ['rate<0.05'],  // max 5% errors during burst
        http_req_duration: ['p(95)<2000'], // relaxed during burst
    },
};

const BASE_URL = __ENV.API_URL || 'http://localhost:8080';

// High-value transaction that should trigger anomaly detection
function fraudulentTransaction() {
    return {
        amount: Math.floor(Math.random() * 50000) + 10000, // €10k–€60k (above threshold)
        currency: 'EUR',
        country: Math.random() > 0.5 ? 'NG' : 'RU',      // geo anomaly
        mcc: '7995',                                        // gambling MCC
    };
}

export default function () {
    // Hammer the transactions endpoint during burst
    const tx = http.get(`${BASE_URL}/api/transactions?page=0&size=10`, {
        tags: { endpoint: 'transactions', scenario: 'fraud_burst' },
    });
    check(tx, { 'tx status 2xx': (r) => r.status >= 200 && r.status < 300 });

    // Also probe anomalies to ensure detection pipeline keeps up
    const anomalies = http.get(`${BASE_URL}/api/anomalies?severity=HIGH`, {
        tags: { endpoint: 'anomalies', scenario: 'fraud_burst' },
    });
    check(anomalies, { 'anomalies status 2xx': (r) => r.status >= 200 && r.status < 300 });

    sleep(0.05);
}
