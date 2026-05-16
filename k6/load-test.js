/**
 * PIA — k6 load test entry point.
 *
 * Runs both scenarios sequentially:
 *   1. normal-flow  : ramp 10→100 rps, 3 min
 *   2. fraud-burst  : spike to 500 rps, 30 s
 *
 * Usage:
 *   k6 run k6/load-test.js -e API_URL=http://<alb-dns>
 *
 * Docker:
 *   docker run --rm -i grafana/k6 run - < k6/load-test.js \
 *     -e API_URL=http://<alb-dns>
 *
 * Prerequisites: API must be running (make deploy + ecs wait services-stable)
 */
import { sleep } from 'k6';
import { check, group } from 'k6';
import http from 'k6/http';

export const options = {
    scenarios: {
        normal_flow: {
            executor: 'ramping-arrival-rate',
            startRate: 10,
            timeUnit: '1s',
            preAllocatedVUs: 20,
            maxVUs: 60,
            stages: [
                { target: 10,  duration: '30s' },
                { target: 50,  duration: '1m' },
                { target: 100, duration: '1m' },
                { target: 100, duration: '1m' },
                { target: 0,   duration: '30s' },
            ],
            exec: 'normalFlow',
        },
        fraud_burst: {
            executor: 'ramping-arrival-rate',
            startTime: '4m',  // starts after normal flow completes
            startRate: 50,
            timeUnit: '1s',
            preAllocatedVUs: 100,
            maxVUs: 200,
            stages: [
                { target: 50,  duration: '15s' },
                { target: 500, duration: '15s' },
                { target: 500, duration: '30s' },
                { target: 50,  duration: '15s' },
                { target: 0,   duration: '15s' },
            ],
            exec: 'fraudBurst',
        },
    },
    thresholds: {
        // Normal flow thresholds
        'http_req_duration{scenario:normal_flow}': ['p(95)<500'],
        'http_req_failed{scenario:normal_flow}':   ['rate<0.01'],
        // Burst thresholds (relaxed)
        'http_req_duration{scenario:fraud_burst}': ['p(95)<2000'],
        'http_req_failed{scenario:fraud_burst}':   ['rate<0.05'],
    },
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

const BASE_URL = __ENV.API_URL || 'http://localhost:8080';

export function normalFlow() {
    group('read api', () => {
        const health = http.get(`${BASE_URL}/actuator/health`);
        check(health, { 'health UP': (r) => r.status === 200 });

        const tx = http.get(`${BASE_URL}/api/transactions?page=0&size=10`);
        check(tx, { 'transactions 200': (r) => r.status === 200 });

        const anomalies = http.get(`${BASE_URL}/api/anomalies`);
        check(anomalies, { 'anomalies 200': (r) => r.status === 200 });

        const metrics = http.get(`${BASE_URL}/api/metrics/summary`);
        check(metrics, { 'metrics 200': (r) => r.status === 200 });
    });
    sleep(0.1);
}

export function fraudBurst() {
    group('burst read', () => {
        const tx = http.get(`${BASE_URL}/api/transactions?page=0&size=5`);
        check(tx, { 'tx 2xx': (r) => r.status >= 200 && r.status < 300 });

        const anomalies = http.get(`${BASE_URL}/api/anomalies?severity=HIGH`);
        check(anomalies, { 'anomalies 2xx': (r) => r.status >= 200 && r.status < 300 });
    });
    sleep(0.05);
}
