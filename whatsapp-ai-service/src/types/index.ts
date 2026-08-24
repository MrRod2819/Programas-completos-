export type MessageRole = 'user' | 'assistant' | 'system';

export interface SessionRecord {
  id: number;
  phone_number: string;
  created_at: Date;
  updated_at: Date;
}

export interface MessageRecord {
  id: number;
  session_id: number;
  role: MessageRole;
  content: string;
  timestamp: Date;
}

export interface LeadRecord {
  id: number;
  phone_number: string;
  name: string | null;
  service_interest: string | null;
  metadata: Record<string, unknown>;
  created_at: Date;
}

export interface ChatMessage {
  role: MessageRole;
  content: string;
}

export interface LeadData {
  name?: string;
  phone?: string;
  service?: string;
  [key: string]: unknown;
}

export interface IncomingTextMessage {
  senderPhone: string;
  text: string;
  messageId: string;
}

export interface WhatsAppTextMessage {
  from: string;
  id: string;
  timestamp: string;
  type: string;
  text?: { body: string };
}

export interface WhatsAppValue {
  messaging_product?: string;
  metadata?: { display_phone_number?: string; phone_number_id?: string };
  contacts?: Array<{ profile?: { name?: string }; wa_id?: string }>;
  messages?: WhatsAppTextMessage[];
  statuses?: Array<{ id: string; status: string }>;
}

export interface WhatsAppWebhookPayload {
  object?: string;
  entry?: Array<{
    id?: string;
    changes?: Array<{ field?: string; value?: WhatsAppValue }>;
  }>;
}
