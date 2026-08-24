import fs from 'fs';
import path from 'path';
import { pool, closePool } from './pool';
import { logger } from '../utils/logger';

// Resolved from the compiled tree (dist/src/db) and from the sources (src/db),
// so the same code works under tsx and inside the Docker image.
const MIGRATIONS_DIR = [
  path.resolve(__dirname, '../../db/migrations'),
  path.resolve(__dirname, '../../../db/migrations'),
  path.resolve(process.cwd(), 'db/migrations'),
].find((candidate) => fs.existsSync(candidate));

export async function runMigrations(): Promise<void> {
  await pool.query(
    `CREATE TABLE IF NOT EXISTS schema_migrations (
       name TEXT PRIMARY KEY,
       applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
     )`
  );

  if (!MIGRATIONS_DIR) {
    throw new Error('migrations directory not found');
  }

  const files = fs
    .readdirSync(MIGRATIONS_DIR)
    .filter((file) => file.endsWith('.sql'))
    .sort();

  for (const file of files) {
    const applied = await pool.query('SELECT 1 FROM schema_migrations WHERE name = $1', [file]);
    if (applied.rowCount && applied.rowCount > 0) {
      continue;
    }
    const sql = fs.readFileSync(path.join(MIGRATIONS_DIR, file), 'utf8');
    const client = await pool.connect();
    try {
      await client.query('BEGIN');
      await client.query(sql);
      await client.query('INSERT INTO schema_migrations (name) VALUES ($1)', [file]);
      await client.query('COMMIT');
      logger.info('migration applied', { file });
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }
}

if (require.main === module) {
  runMigrations()
    .then(() => closePool())
    .then(() => process.exit(0))
    .catch((error: unknown) => {
      logger.error('migration failed', { error: String(error) });
      process.exit(1);
    });
}
