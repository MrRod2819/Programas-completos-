import dotenv from 'dotenv';

dotenv.config();

function optional(name: string, fallback: string): string {
  const value = process.env[name];
  return value === undefined || value === '' ? fallback : value;
}

export interface AppConfig {
  port: number;
  databaseUrl: string;
  metaAccessToken: string;
  metaPhoneNumberId: string;
  metaAppSecret: string;
  webhookVerifyToken: string;
  openaiApiKey: string;
  openaiModel: string;
  graphApiVersion: string;
  mockMode: boolean;
  contextMessageLimit: number;
}

export const config: AppConfig = {
  port: Number(optional('PORT', '3000')),
  databaseUrl: optional('DATABASE_URL', 'postgres://whatsapp:whatsapp@localhost:5432/whatsapp_ai'),
  metaAccessToken: optional('META_ACCESS_TOKEN', ''),
  metaPhoneNumberId: optional('META_PHONE_NUMBER_ID', ''),
  metaAppSecret: optional('META_APP_SECRET', ''),
  webhookVerifyToken: optional('WEBHOOK_VERIFY_TOKEN', 'my-verify-token'),
  openaiApiKey: optional('OPENAI_API_KEY', ''),
  openaiModel: optional('OPENAI_MODEL', 'gpt-4o-mini'),
  graphApiVersion: optional('GRAPH_API_VERSION', 'v19.0'),
  mockMode: optional('MOCK_MODE', 'false').toLowerCase() === 'true',
  contextMessageLimit: Number(optional('CONTEXT_MESSAGE_LIMIT', '6')),
};
