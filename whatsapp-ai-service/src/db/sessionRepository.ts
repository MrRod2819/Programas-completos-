import { pool } from './pool';
import { SessionRecord } from '../types';

export async function getOrCreateSession(phoneNumber: string): Promise<SessionRecord> {
  const result = await pool.query<SessionRecord>(
    `INSERT INTO sessions (phone_number)
     VALUES ($1)
     ON CONFLICT (phone_number)
     DO UPDATE SET updated_at = NOW()
     RETURNING id, phone_number, created_at, updated_at`,
    [phoneNumber]
  );
  return result.rows[0];
}

export async function touchSession(sessionId: number): Promise<void> {
  await pool.query('UPDATE sessions SET updated_at = NOW() WHERE id = $1', [sessionId]);
}
