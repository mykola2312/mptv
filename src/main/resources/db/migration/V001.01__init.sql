CREATE TABLE source (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    type        TEXT    NOT NULL,
    root_name   TEXT    NOT NULL,
    url         TEXT,
    path        TEXT,
    cookies     TEXT
);

CREATE UNIQUE INDEX idx_source_url_path ON source(url,path);

CREATE TABLE crawl (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    crawled_at  INTEGER NOT NULL
);

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
    crawl       INTEGER NOT NULL,

    FOREIGN KEY (category)  REFERENCES category(id),
    FOREIGN KEY (crawl)     REFERENCES crawl(id)
);

CREATE UNIQUE INDEX idx_channel_category_title ON channel(category,title);

CREATE TABLE task (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    last_time   INTEGER NOT NULL
);

CREATE UNIQUE INDEX idx_task_name ON task(name);