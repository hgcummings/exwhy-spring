CREATE TABLE contributor (
    id          serial PRIMARY KEY NOT NULL,
    name        varchar(24) NOT NULL,
    realname    varchar(48),
    email       varchar(255),
    open_id     varchar(255) NOT NULL,
    registered  timestamp DEFAULT current_timestamp NOT NULL);