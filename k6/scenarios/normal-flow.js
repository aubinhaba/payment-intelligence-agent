import { sleep } from 'k6';
import { check } from 'k6';
import http from 'k6/http';
import { generatePaymentEvent } from '../lib/event-generator.js';

// Normal traffic: 10 → 100 rps over 3 minutes, then steady at 100 rps
export const options = {
    scenarios: {
        normal_ramp: {
            executor: 'ramping-arrival-rate',
            startRate: 10,
            timeUnit: '1s',
            preAllocatedVUs: 20,
            maxVUs: 50,
            stages: [
                { target: 10,  duration: '30s' },  // warm up
                { target: 50,  duration: '1m' },   // ramp up
                { target: 100, duration: '1m' },   // target load
                { target: 100, duration: '1m' },   // steady state
                { target: 0,   duration: '30s' },  // cool down
            ],
        },
    },
    thresholds: {
        http_req_duration:            ['p(95)<500', 'p(99)<1000'],
        http_req_failed:              ['rate<0.01'],
        'http_req_duration{endpoint:health}':       ['p(95)<100'],
        'http_req_duration{endpoint:transactions}': ['p(95)<600'],
        'http_req_duration{endpoint:anomalies}':    ['p(95)<600'],
    },
};

const BASE_URL = __ENV.API_URL || 'http://localhost:8080';

export default function () {
    // Health check (lightweight, baseline)
    const health = http.get(`${BASE_URL}/actuator/health`, {
        tags: { endpoint: 'health' },
    });
    check(health, { 'health is UP': (r) => r.status === 200 });

    // Read transactions list
    const tx = http.get(`${BASE_URL}/api/transactions?page=0&size=20`, {
        tags: { endpoint: 'transactions' },
    });
    check(tx, {
        'transactions 200': (r) => r.status === 200,
        'transactions has content': (r) => JSON.parse(r.body).content !== undefined,
    });

    // Read anomalies list
    const anomalies = http.get(`${BASE_URL}/api/anomalies`, {
        tags: { endpoint: 'anomalies' },
    });
    check(anomalies, { 'anomalies 200': (r) => r.status === 200 });

    // Read metrics summary
    const metrics = http.get(`${BASE_URL}/api/metrics/summary`, {
        tags: { endpoint: 'metrics' },
    });
    check(metrics, { 'metrics 200': (r) => r.status === 200 });

    sleep(0.1);
}
