import crypto from 'crypto';
import { describe, expect, it } from 'vitest';
import { isValidSignature } from '../src/utils/signature';

const SECRET = 'app-secret';
const BODY = Buffer.from('{"object":"whatsapp_business_account"}');

function sign(body: Buffer, secret: string): string {
  return `sha256=${crypto.createHmac('sha256', secret).update(body).digest('hex')}`;
}

describe('isValidSignature', () => {
  it('accepts a signature generated with the app secret', () => {
    expect(isValidSignature(BODY, sign(BODY, SECRET), SECRET)).toBe(true);
  });

  it('rejects a signature generated with another secret', () => {
    expect(isValidSignature(BODY, sign(BODY, 'other'), SECRET)).toBe(false);
  });

  it('rejects a tampered body', () => {
    expect(isValidSignature(Buffer.from('{}'), sign(BODY, SECRET), SECRET)).toBe(false);
  });

  it('rejects a missing or malformed header', () => {
    expect(isValidSignature(BODY, undefined, SECRET)).toBe(false);
    expect(isValidSignature(BODY, 'sha1=abc', SECRET)).toBe(false);
  });
});
