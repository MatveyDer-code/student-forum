-- Создаём таблицу ролей
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Создаём таблицу пользователей
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role_id INT NOT NULL REFERENCES roles(id)
);

-- Добавляем три роли
INSERT INTO roles (name) VALUES
('STUDENT'),
('MODERATOR'),
('TEACHER');

-- Добавляем пользователей
INSERT INTO users (username, password, email, role_id) VALUES
('alice', '$2a$12$Ya8bRPqlMTlZltDeU35v.uwz5Yia9YKX12azbVm0qLdFUPqxZ8XQO', 'alice@example.com', 1),   -- студент
('bob', '$2a$12$jgknxATx63nkxLXs9PYQ9OTKZkSGWNJlWL/9l5AOj57WiFOC0pfpG', 'bob@example.com', 2),       -- модератор
('charlie', '$2a$12$nkUHkwvv2djge3VayDo5yO5oTfYBc280wVIis2sKRlsNKgyOYA8dS', 'charlie@example.com', 3); -- преподаватель