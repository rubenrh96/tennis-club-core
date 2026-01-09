ALTER TABLE players
    ALTER COLUMN phase_month TYPE VARCHAR(7)
    USING TO_CHAR(phase_month, 'YYYY-MM');