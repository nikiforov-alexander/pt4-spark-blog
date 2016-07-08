CREATE TABLE IF NOT EXISTS entries (
  id INTEGER PRIMARY KEY auto_increment,
  title VARCHAR,
  body VARCHAR,
  date DATETIME
);

CREATE TABLE IF NOT EXISTS comments (
  id INTEGER PRIMARY KEY auto_increment,
  entry_id INTEGER,
  author VARCHAR,
  body VARCHAR,
  date DATETIME,
  FOREIGN KEY(entry_id) REFERENCES PUBLIC.entries(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tags (
  id INTEGER PRIMARY KEY auto_increment,
  entry_id INTEGER,
  name VARCHAR,
  FOREIGN KEY(entry_id) REFERENCES PUBLIC.entries(id) ON DELETE CASCADE
);
