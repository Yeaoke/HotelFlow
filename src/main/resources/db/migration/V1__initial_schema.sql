CREATE TABLE users (
    id uuid PRIMARY KEY, username varchar(255) NOT NULL UNIQUE, password varchar(255) NOT NULL,
    role varchar(255), firstname varchar(255) DEFAULT 'firstname', lastname varchar(255) DEFAULT 'lastname',
    email varchar(255) UNIQUE, phone_number varchar(255), is_verified boolean NOT NULL
);
CREATE TABLE rooms (
    id uuid PRIMARY KEY, owner_id uuid NOT NULL REFERENCES users(id),
    home_type varchar(255) NOT NULL, address varchar(255) NOT NULL,
    has_tv boolean NOT NULL, has_internet boolean NOT NULL, has_kitchen boolean NOT NULL, has_air_con boolean NOT NULL,
    price bigint, latitude double precision, longitude double precision
);
CREATE TABLE reservations (
    id uuid PRIMARY KEY, user_id uuid NOT NULL REFERENCES users(id), room_id uuid NOT NULL REFERENCES rooms(id),
    start_date date NOT NULL, end_date date NOT NULL, price bigint NOT NULL, status smallint NOT NULL
);
CREATE TABLE reviews (
    id uuid PRIMARY KEY, reservation_id uuid NOT NULL REFERENCES reservations(id), rating integer
);
CREATE TABLE refresh_tokens (
    id uuid PRIMARY KEY, token_hash varchar(255) NOT NULL UNIQUE,
    user_id uuid NOT NULL REFERENCES users(id), logged_out boolean NOT NULL DEFAULT false
);
