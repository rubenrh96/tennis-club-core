-- Añade columna para guardar el grupo en el momento del partido
ALTER TABLE matches
    ADD COLUMN IF NOT EXISTS group_no_at_match INTEGER;

