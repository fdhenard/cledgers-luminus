CREATE TABLE users
(id SERIAL PRIMARY KEY,
 username VARCHAR(30) unique not null,
 first_name VARCHAR(30) not null,
 last_name VARCHAR(30) not null,
 email VARCHAR(30) not null,
 admin BOOLEAN not null default(false),
 last_login TIME,
 is_active BOOLEAN not null default(false),
 pass VARCHAR(300) not null);
