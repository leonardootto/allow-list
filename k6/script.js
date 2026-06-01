import http from 'k6/http';
import { check } from 'k6';

export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(50)', 'p(90)', 'p(95)', 'p(99)'],
  stages: [
    { duration: '30s', target: 100 },
    { duration: '2m',  target: 100 },
    { duration: '15s', target: 0   },
  ],
  thresholds: {
    'http_req_duration': ['p(99)<20'],
    'http_req_failed':   ['rate<0.001'],
  },
};

export default function () {
  const isHit = Math.random() < 0.7;
  // hits:   PVs 1–500000 (present in the queried `merchants` list)
  // misses: PVs 1000001–1500000 (absent from `merchants`; they belong to
  //         `merchants-3`, but we only ever query `merchants`, so they miss)
  const pv = isHit
    ? Math.floor(Math.random() * 500_000) + 1
    : Math.floor(Math.random() * 500_000) + 1_000_001;

  // name tag collapses all PV variants into one time series to avoid cardinality explosion
  const res = http.get(`http://app:8080/allow-list/merchants?pv=${pv}`, {
    tags: { name: 'check_merchant_pv' },
  });
  check(res, { 'status 200': (r) => r.status === 200 });
}

export function handleSummary(data) {
  const d = data.metrics;
  const dur = d.http_req_duration.values;
  return {
    '/results/summary.json': JSON.stringify({
      p50_ms:         dur['p(50)'] ?? dur['med'] ?? null,
      p95_ms:         dur['p(95)'] ?? null,
      p99_ms:         dur['p(99)'] ?? null,
      error_rate:     d.http_req_failed.values.rate,
      requests_total: d.http_reqs.values.count,
      rps:            d.http_reqs.values.rate,
    }, null, 2),
  };
}
