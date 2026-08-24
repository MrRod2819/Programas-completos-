import { pool } from './pool';
import { LeadData, LeadRecord } from '../types';

export async function saveLead(phoneNumber: string, lead: LeadData): Promise<LeadRecord> {
  const name = typeof lead.name === 'string' && lead.name.trim() !== '' ? lead.name.trim() : null;
  const service =
    typeof lead.service === 'string' && lead.service.trim() !== '' ? lead.service.trim() : null;

  const result = await pool.query<LeadRecord>(
    `INSERT INTO leads (phone_number, name, service_interest, metadata)
     VALUES ($1, $2, $3, $4::jsonb)
     RETURNING id, phone_number, name, service_interest, metadata, created_at`,
    [phoneNumber, name, service, JSON.stringify(lead)]
  );
  return result.rows[0];
}
