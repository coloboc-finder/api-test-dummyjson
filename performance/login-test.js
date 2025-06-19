import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 50,
  duration: '30s',
};

export default function () {
  const payload = JSON.stringify({
    username: 'emilys',
    password: 'emilyspass'
  });

  const headers = { 'Content-Type': 'application/json' };

  const res = http.post('https://dummyjson.com/auth/login', payload, { headers });

  check(res, {
    'Статус 200': (r) => r.status === 200,
    'Есть токен': (r) => r.json('accessToken') !== undefined,
    'Время < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
