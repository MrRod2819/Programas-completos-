import { pool } from './pool';
import { ChatMessage, MessageRecord, MessageRole } from '../types';

export async function saveMessage(
  sessionId: number,
  role: MessageRole,
  content: string
): Promise<MessageRecord> {
  const result = await pool.query<MessageRecord>(
    `INSERT INTO messages (session_id, role, content)
     VALUES ($1, $2, $3)
     RETURNING id, session_id, role, content, timestamp`,
    [sessionId, role, content]
  );
  return result.rows[0];
}

export async function getRecentMessages(sessionId: number, limit: number): Promise<ChatMessage[]> {
  const result = await pool.query<Pick<MessageRecord, 'role' | 'content'>>(
    `SELECT role, content
     FROM (
       SELECT role, content, timestamp, id
       FROM messages
       WHERE session_id = $1
       ORDER BY timestamp DESC, id DESC
       LIMIT $2
     ) recent
     ORDER BY recent.timestamp ASC, recent.id ASC`,
    [sessionId, limit]
  );
  return result.rows.map((row) => ({ role: row.role, content: row.content }));
}
