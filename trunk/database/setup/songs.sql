CREATE TABLE songs
(
   ID INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
   TITLE VARCHAR(100) NOT NULL,
   NOTES CLOB,
   ADDED_DATE TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
   SEARCHABLE_TITLE GENERATED ALWAYS AS (UCASE(TITLE)),
   PRIMARY KEY (ID)
)

CREATE TABLE song_parts
(
   ID INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
   SONG_ID INT NOT NULL,
   PART_TYPE CHAR(1) NOT NULL DEFAULT 'C',
   PART_INDEX INT NOT NULL DEFAULT 1,
   ORDER_BY INT NOT NULL DEFAULT 1,
   FONT_SIZE INT NOT NULL DEFAULT 40,
   TEXT CLOB,
   SEARCHABLE_TEXT GENERATED ALWAYS AS (UCASE(TEXT)),
   PRIMARY KEY (ID)
)
ALTER TABLE song_parts ADD FOREIGN KEY (SONG_ID) REFERENCES songs(ID)