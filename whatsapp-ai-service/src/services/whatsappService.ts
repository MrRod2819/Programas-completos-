import axios from 'axios';
import { config } from '../config/env';
import { logger } from '../utils/logger';

export async function sendTextMessage(to: string, body: string): Promise<void> {
  if (config.mockMode || config.metaAccessToken === '' || config.metaPhoneNumberId === '') {
    logger.warn('whatsapp send skipped (mock mode or missing credentials)', { to, body });
    return;
  }

  const url = `https://graph.facebook.com/${config.graphApiVersion}/${config.metaPhoneNumberId}/messages`;

  try {
    await axios.post(
      url,
      {
        messaging_product: 'whatsapp',
        recipient_type: 'individual',
        to,
        type: 'text',
        text: { preview_url: false, body },
      },
      {
        headers: {
          Authorization: `Bearer ${config.metaAccessToken}`,
          'Content-Type': 'application/json',
        },
        timeout: 15_000,
      }
    );
    logger.info('whatsapp message sent', { to });
  } catch (error) {
    logger.error('whatsapp send failed', { to, error: String(error) });
  }
}
