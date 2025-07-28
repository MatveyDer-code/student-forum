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
('alice', '{noop}password1', 'alice@example.com', 1),   -- студент
('bob', '{noop}password2', 'bob@example.com', 2),       -- модератор
('charlie', '{noop}password3', 'charlie@example.com', 3); -- преподаватель