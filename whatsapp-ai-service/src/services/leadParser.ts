import { LeadData } from '../types';

const LEAD_TAG_REGEX = /\[LEAD_DATA:\s*(\{[\s\S]*?\})\s*\]/;

export interface ParsedReply {
  reply: string;
  lead: LeadData | null;
}

export function parseLeadData(rawReply: string): ParsedReply {
  const match = rawReply.match(LEAD_TAG_REGEX);
  if (!match) {
    return { reply: rawReply.trim(), lead: null };
  }

  const reply = rawReply.replace(LEAD_TAG_REGEX, '').replace(/\n{3,}/g, '\n\n').trim();

  try {
    const parsed: unknown = JSON.parse(match[1]);
    if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
      return { reply, lead: null };
    }
    return { reply, lead: parsed as LeadData };
  } catch {
    return { reply, lead: null };
  }
}
