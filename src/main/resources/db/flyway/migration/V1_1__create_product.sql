CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS product
(
    ID uuid PRIMARY KEY UNIQUE DEFAULT uuid_generate_v4(),
    NAME varchar(255) NOT NULL,
    DESCRIPTION varchar(255) NOT NULL,
    RATE float8 NOT NULL,
    IMAGE varchar(255),
    DISCOUNT_ID uuid
);

CREATE TABLE IF NOT EXISTS tags
(
    ID uuid PRIMARY KEY UNIQUE DEFAULT uuid_generate_v4(),
    NAME varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS discount
(
    ID uuid PRIMARY KEY UNIQUE DEFAULT uuid_generate_v4(),
    FACTOR varchar(255),
    ACTIVE bit NOT NULL
);

CREATE TABLE IF NOT EXISTS discount_rate
(
    ID uuid PRIMARY KEY UNIQUE DEFAULT uuid_generate_v4(),
    RATE float8 NOT NULL,
    SLAB int4
);