-- No data is deleted or rescaled. Invalid legacy rows must be repaired deliberately.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM reservations WHERE status NOT BETWEEN 0 AND 3) THEN
        RAISE EXCEPTION 'Unknown legacy reservation status. Inspect reservations before migrating.';
    END IF;
    IF EXISTS (SELECT 1 FROM reviews WHERE rating IS NULL OR rating NOT BETWEEN 1 AND 5) THEN
        RAISE EXCEPTION 'Legacy reviews must use rating 1..5. Resolve invalid ratings before migrating.';
    END IF;
    IF EXISTS (SELECT 1 FROM reviews GROUP BY reservation_id HAVING COUNT(*) > 1) THEN
        RAISE EXCEPTION 'Multiple reviews per reservation. Resolve duplicates before migrating.';
    END IF;
    IF EXISTS (SELECT 1 FROM users WHERE email IS NULL OR btrim(email) = '') THEN
        RAISE EXCEPTION 'Users require email addresses before enabling email verification.';
    END IF;
    IF EXISTS (SELECT 1 FROM users GROUP BY lower(btrim(email)) HAVING COUNT(*) > 1) THEN
        RAISE EXCEPTION 'Duplicate normalized emails. Resolve duplicates before migrating.';
    END IF;
END $$;
DO $$
DECLARE c record;
BEGIN
    FOR c IN SELECT conname FROM pg_constraint
        WHERE conrelid='reservations'::regclass AND contype='c'
        AND pg_get_constraintdef(oid) LIKE '%status%'
    LOOP
        EXECUTE format('ALTER TABLE reservations DROP CONSTRAINT %I',c.conname);
    END LOOP;
END $$;
-- Legacy FREE (ordinal 3) means the dates are released, so preserve it as CANCELED.
ALTER TABLE reservations ALTER COLUMN status TYPE varchar(255) USING
    CASE status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'CANCELED' WHEN 3 THEN 'CANCELED' END;
ALTER TABLE reservations ADD CONSTRAINT reservation_status CHECK (status IN ('PENDING','APPROVED','CANCELED','COMPLETED'));
ALTER TABLE reservations ADD CONSTRAINT reservation_dates CHECK (end_date > start_date AND end_date-start_date BETWEEN 1 AND 30);
ALTER TABLE reservations ADD CONSTRAINT reservation_price CHECK (price >= 0);
ALTER TABLE rooms ALTER COLUMN price SET NOT NULL;
ALTER TABLE rooms ADD CONSTRAINT room_price CHECK (price >= 0);
ALTER TABLE rooms ADD CONSTRAINT room_coordinates CHECK (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180);
ALTER TABLE reviews ALTER COLUMN rating SET NOT NULL;
ALTER TABLE reviews ADD CONSTRAINT review_rating CHECK (rating BETWEEN 1 AND 5);
ALTER TABLE reviews ADD CONSTRAINT one_review_per_reservation UNIQUE (reservation_id);
UPDATE users SET email=lower(btrim(email)), role=COALESCE(role,'USER');
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
CREATE UNIQUE INDEX users_normalized_email ON users(lower(email));
-- Revoke pre-migration sessions. JWT subject and session claims have changed.
ALTER TABLE refresh_tokens ADD COLUMN expires_at timestamp(6) with time zone NOT NULL DEFAULT '1970-01-01 00:00:00+00';
ALTER TABLE refresh_tokens ADD COLUMN access_hash varchar(255);
UPDATE refresh_tokens SET logged_out=true;
ALTER TABLE refresh_tokens ALTER COLUMN expires_at DROP DEFAULT;
CREATE INDEX refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX reservation_user_dates ON reservations(user_id,start_date);
CREATE INDEX room_price_index ON rooms(price,id);
CREATE EXTENSION IF NOT EXISTS btree_gist;
-- PostgreSQL enforces this even for writes that bypass the Java service.
ALTER TABLE reservations ADD CONSTRAINT no_overlapping_reservations
    EXCLUDE USING gist (room_id WITH =, daterange(start_date,end_date,'[)') WITH &&)
    WHERE (status <> 'CANCELED');
