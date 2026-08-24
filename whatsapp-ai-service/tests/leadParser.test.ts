import { describe, expect, it } from 'vitest';
import { parseLeadData } from '../src/services/leadParser';

describe('parseLeadData', () => {
  it('returns the reply untouched when there is no lead tag', () => {
    const result = parseLeadData('Hola, la limpieza dental cuesta S/ 120.');
    expect(result.lead).toBeNull();
    expect(result.reply).toBe('Hola, la limpieza dental cuesta S/ 120.');
  });

  it('extracts the lead and strips the tag from the reply', () => {
    const raw =
      'Listo Rodrigo, tu cita quedo agendada.\n[LEAD_DATA: {"name": "Rodrigo", "phone": "51987654321", "service": "limpieza dental"}]';
    const result = parseLeadData(raw);
    expect(result.reply).toBe('Listo Rodrigo, tu cita quedo agendada.');
    expect(result.lead).toEqual({
      name: 'Rodrigo',
      phone: '51987654321',
      service: 'limpieza dental',
    });
  });

  it('strips the tag but returns no lead when the JSON is malformed', () => {
    const result = parseLeadData('Gracias. [LEAD_DATA: {name: Rodrigo}]');
    expect(result.reply).toBe('Gracias.');
    expect(result.lead).toBeNull();
  });
});
