CREATE TABLE marriages (
    first_partner_id VARCHAR(50) NOT NULL,
    second_partner_id VARCHAR(50) NOT NULL,
    married_at BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE users ADD COLUMN has_ring BOOLEAN NOT NULL DEFAULT FALSE;