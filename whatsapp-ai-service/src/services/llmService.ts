import axios from 'axios';
import { config } from '../config/env';
import { buildSystemPrompt } from '../config/systemPrompt';
import { ChatMessage } from '../types';
import { logger } from '../utils/logger';

const OPENAI_URL = 'https://api.openai.com/v1/chat/completions';

interface OpenAIChoice {
  message?: { content?: string };
}

interface OpenAIResponse {
  choices?: OpenAIChoice[];
}

const FALLBACK_REPLY =
  'Gracias por escribirnos. En breve un asesor se comunicara contigo para ayudarte.';

function mockReply(history: ChatMessage[]): string {
  const lastUserMessage = [...history].reverse().find((message) => message.role === 'user');
  const text = (lastUserMessage?.content ?? '').toLowerCase();
  const wantsAppointment =
    text.includes('cita') || text.includes('reserva') || text.includes('agendar');
  const base = wantsAppointment
    ? 'Perfecto, agendamos tu cita. Te esperamos en Av. Larco 1234, Miraflores.'
    : 'Gracias por escribirnos. Atendemos de lunes a viernes de 9:00 a 19:00 y sabados de 9:00 a 13:00.';
  if (!wantsAppointment) {
    return base;
  }
  return `${base} [LEAD_DATA: {"name": "", "phone": "", "service": "cita"}]`;
}

export async function generateReply(history: ChatMessage[]): Promise<string> {
  if (config.mockMode || config.openaiApiKey === '') {
    logger.warn('llm running in mock mode');
    return mockReply(history);
  }

  const messages: ChatMessage[] = [
    { role: 'system', content: buildSystemPrompt() },
    ...history,
  ];

  try {
    const response = await axios.post<OpenAIResponse>(
      OPENAI_URL,
      { model: config.openaiModel, messages, temperature: 0.4, max_tokens: 300 },
      {
        headers: {
          Authorization: `Bearer ${config.openaiApiKey}`,
          'Content-Type': 'application/json',
        },
        timeout: 30_000,
      }
    );
    const content = response.data.choices?.[0]?.message?.content?.trim();
    return content && content !== '' ? content : FALLBACK_REPLY;
  } catch (error) {
    logger.error('llm request failed', { error: String(error) });
    return FALLBACK_REPLY;
  }
}
