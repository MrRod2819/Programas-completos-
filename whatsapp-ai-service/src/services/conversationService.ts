import { config } from '../config/env';
import { getRecentMessages, saveMessage } from '../db/messageRepository';
import { saveLead } from '../db/leadRepository';
import { getOrCreateSession, touchSession } from '../db/sessionRepository';
import { ChatMessage, IncomingTextMessage, LeadData } from '../types';
import { logger } from '../utils/logger';
import { generateReply } from './llmService';
import { parseLeadData } from './leadParser';
import { sendTextMessage } from './whatsappService';

export interface ConversationResult {
  reply: string;
  lead: LeadData | null;
}

export async function handleIncomingMessage(
  incoming: IncomingTextMessage
): Promise<ConversationResult> {
  const session = await getOrCreateSession(incoming.senderPhone);

  const history = await getRecentMessages(session.id, config.contextMessageLimit);
  await saveMessage(session.id, 'user', incoming.text);

  const context: ChatMessage[] = [...history, { role: 'user', content: incoming.text }];
  const rawReply = await generateReply(context);
  const { reply, lead } = parseLeadData(rawReply);

  if (lead) {
    const phone = typeof lead.phone === 'string' && lead.phone.trim() !== ''
      ? lead.phone.trim()
      : incoming.senderPhone;
    const stored = await saveLead(phone, lead);
    logger.info('lead saved', { leadId: stored.id, phone });
  }

  await sendTextMessage(incoming.senderPhone, reply);
  await saveMessage(session.id, 'assistant', reply);
  await touchSession(session.id);

  return { reply, lead };
}
