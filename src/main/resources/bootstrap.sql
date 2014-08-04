CREATE TABLE contributor (
    id          serial PRIMARY KEY NOT NULL,
    name        varchar(24) NOT NULL,
    realname    varchar(48),
    email       varchar(255),
    open_id     varchar(255) NOT NULL,
    registered  timestamp DEFAULT current_timestamp NOT NULL);

CREATE TABLE item (
    id  serial PRIMARY KEY NOT NULL,
    x   integer NOT NULL DEFAULT 0,
    y   integer NOT NULL DEFAULT 0);

CREATE OR REPLACE FUNCTION insert_item() RETURNS trigger AS $BODY$
    BEGIN
        IF TG_OP='INSERT' THEN
            INSERT INTO item DEFAULT VALUES RETURNING id INTO NEW.item_id;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql VOLATILE;

CREATE TABLE post (
    item_id           integer NOT NULL,
    title             varchar( 56 ),
    body              text NOT NULL,
    contributor_id    integer NOT NULL,
    date              timestamp DEFAULT current_timestamp NOT NULL,
    read              bool DEFAULT FALSE,
    edited            bool NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_votable_id PRIMARY KEY(item_id),
    CONSTRAINT fk__item_id__references__item__id FOREIGN KEY( item_id ) REFERENCES item ( id ),
    CONSTRAINT fk__post__contributor_id__references__contributor__id FOREIGN KEY( contributor_id ) REFERENCES contributor ( id )
);

CREATE TRIGGER insert_post
  BEFORE INSERT
  ON post
  FOR EACH ROW
  EXECUTE PROCEDURE insert_item();