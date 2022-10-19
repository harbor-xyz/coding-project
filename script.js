import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
  http.get('http://172.17.0.1:8080/hello?toUser=Priyesh');
  sleep(1);
}
