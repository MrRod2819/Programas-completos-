import crypto from 'crypto';

/**
 * Constant-time comparison of Meta's X-Hub-Signature-256 header against the
 * HMAC-SHA256 of the raw request body.
 */
export function isValidSignature(
  rawBody: Buffer,
  header: string | undefined,
  appSecret: string
): boolean {
  if (!header || !header.startsWith('sha256=')) {
    return false;
  }
  const expected = Buffer.from(
    `sha256=${crypto.createHmac('sha256', appSecret).update(rawBody).digest('hex')}`
  );
  const received = Buffer.from(header);
  if (expected.length !== received.length) {
    return false;
  }
  return crypto.timingSafeEqual(expected, received);
}
