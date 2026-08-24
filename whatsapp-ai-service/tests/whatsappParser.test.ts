import fs from 'fs';
import path from 'path';
import { describe, expect, it } from 'vitest';
import { extractTextMessage } from '../src/services/whatsappParser';

function payload(name: string): unknown {
  return JSON.parse(
    fs.readFileSync(path.resolve(__dirname, '../scripts/payloads', `${name}.json`), 'utf8')
  );
}

describe('extractTextMessage', () => {
  it('extracts sender and text from a text message payload', () => {
    const result = extractTextMessage(payload('textMessage') as never);
    expect(result).toEqual({
      senderPhone: '51987654321',
      text: 'Hola, cuanto cuesta una limpieza dental?',
      messageId: 'wamid.TEXT1',
    });
  });

  it('ignores delivery status updates', () => {
    expect(extractTextMessage(payload('statusUpdate') as never)).toBeNull();
  });

  it('ignores non-text messages', () => {
    expect(extractTextMessage(payload('imageMessage') as never)).toBeNull();
  });

  it('ignores empty payloads', () => {
    expect(extractTextMessage({})).toBeNull();
  });
});
