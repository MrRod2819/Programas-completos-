type LogPayload = Record<string, unknown>;

function emit(level: string, message: string, payload?: LogPayload): void {
  const entry = { level, message, time: new Date().toISOString(), ...payload };
  const line = JSON.stringify(entry);
  if (level === 'error') {
    console.error(line);
  } else {
    console.log(line);
  }
}

export const logger = {
  info: (message: string, payload?: LogPayload): void => emit('info', message, payload),
  warn: (message: string, payload?: LogPayload): void => emit('warn', message, payload),
  error: (message: string, payload?: LogPayload): void => emit('error', message, payload),
};
