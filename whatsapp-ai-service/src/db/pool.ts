import { Pool } from 'pg';
import { config } from '../config/env';

export const pool = new Pool({ connectionString: config.databaseUrl });

export async function closePool(): Promise<void> {
  await pool.end();
}
