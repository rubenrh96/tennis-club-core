-- Migración de datos desde el modelo antiguo basado en YearMonth ("YYYY-MM")
-- al nuevo identificador funcional de fase (phaseCode) con formato "YYYY-F".
--
-- Estrategia:
--   - Para registros existentes donde phase_month sigue en formato "YYYY-MM",
--     usamos el mes como número de fase (enero=1, febrero=2, ...).
--   - El resultado se guarda en la misma columna phase_month, ahora interpretada
--     como phaseCode.

UPDATE players
SET phase_month = CONCAT(
        SUBSTRING(phase_month, 1, 4),
        '-',
        CAST(SUBSTRING(phase_month, 6, 2) AS INTEGER)
    )
WHERE phase_month IS NOT NULL
  AND phase_month ~ '^\d{4}-\d{2}$';

UPDATE matches
SET phase_month = CONCAT(
        SUBSTRING(phase_month, 1, 4),
        '-',
        CAST(SUBSTRING(phase_month, 6, 2) AS INTEGER)
    )
WHERE phase_month IS NOT NULL
  AND phase_month ~ '^\d{4}-\d{2}$';

