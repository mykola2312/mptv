CREATE TABLE category (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT    NOT NULL
);

CREATE UNIQUE INDEX idx_category_title ON category(title);

CREATE TABLE channel (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    category    INTEGER NOT NULL,
    title       TEXT    NOT NULL,
    url         TEXT    NOT NULL,
    logo        TEXT,

    FOREIGN KEY (category) REFERENCES category(id)
);

CREATE UNIQUE INDEX idx_channel_category_title ON channel(category,title);