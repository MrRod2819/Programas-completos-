/**
 * Simulates Meta WhatsApp webhook traffic against a locally running service.
 *
 *   npm run mock:webhook                 # sends every payload in scripts/payloads
 *   npm run mock:webhook -- textMessage  # sends a single payload by file name
 *
 * Set BASE_URL to target a different host (default http://localhost:3000).
 */
import fs from 'fs';
import path from 'path';
import axios from 'axios';
import { config } from '../src/config/env';

const BASE_URL = process.env.BASE_URL ?? `http://localhost:${config.port}`;
const PAYLOAD_DIR = path.resolve(__dirname, 'payloads');

async function verifyEndpoint(): Promise<void> {
  const challenge = 'challenge-12345';
  const response = await axios.get(`${BASE_URL}/webhook`, {
    params: {
      'hub.mode': 'subscribe',
      'hub.verify_token': config.webhookVerifyToken,
      'hub.challenge': challenge,
    },
    validateStatus: () => true,
  });
  const ok = response.status === 200 && response.data === challenge;
  console.log(`GET  /webhook  -> ${response.status} ${ok ? 'OK (challenge echoed)' : 'FAILED'}`);
  if (!ok) {
    process.exitCode = 1;
  }
}

async function sendPayload(file: string): Promise<void> {
  const body = JSON.parse(fs.readFileSync(path.join(PAYLOAD_DIR, file), 'utf8'));
  const response = await axios.post(`${BASE_URL}/webhook`, body, { validateStatus: () => true });
  const ok = response.status === 200;
  console.log(`POST /webhook  -> ${response.status} ${ok ? 'OK' : 'FAILED'}  (${file})`);
  if (!ok) {
    process.exitCode = 1;
  }
}

async function main(): Promise<void> {
  const requested = process.argv.slice(2);
  const files =
    requested.length > 0
      ? requested.map((name) => (name.endsWith('.json') ? name : `${name}.json`))
      : fs.readdirSync(PAYLOAD_DIR).filter((file) => file.endsWith('.json')).sort();

  await verifyEndpoint();
  for (const file of files) {
    await sendPayload(file);
    // give the async pipeline (db + llm + dispatch) time to finish before the next one
    await new Promise((resolve) => setTimeout(resolve, 1500));
  }
}

main().catch((error: unknown) => {
  console.error('mock webhook runner failed:', String(error));
  process.exit(1);
});
