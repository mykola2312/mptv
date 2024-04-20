CREATE TABLE category (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT    NOT NULL
);

CREATE INDEX idx_category_title ON category(title);

CREATE TABLE channel (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    category    INTEGER NOT NULL,
    title       TEXT    NOT NULL,
    url         TEXT    NOT NULL,
    logo        TEXT,

    FOREIGN KEY (category) REFERENCES category(id)
);

CREATE INDEX idx_channel_title ON channel(title);