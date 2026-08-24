import { IncomingTextMessage, WhatsAppWebhookPayload } from '../types';

export function extractTextMessage(payload: WhatsAppWebhookPayload): IncomingTextMessage | null {
  const value = payload?.entry?.[0]?.changes?.[0]?.value;
  if (!value) {
    return null;
  }
  if (value.statuses && value.statuses.length > 0) {
    return null;
  }
  const message = value.messages?.[0];
  if (!message || message.type !== 'text') {
    return null;
  }
  const text = message.text?.body?.trim();
  if (!text || !message.from) {
    return null;
  }
  return { senderPhone: message.from, text, messageId: message.id };
}
