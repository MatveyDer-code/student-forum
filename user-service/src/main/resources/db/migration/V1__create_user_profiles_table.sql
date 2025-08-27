CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    auth_user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    group_number VARCHAR(50),
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO user_profiles (auth_user_id, first_name, last_name, group_number, phone_number)
VALUES
(1, 'Алиса', 'Иванова', 'ИС-301', '+79990001122'),
(2, 'Петр', 'Петров', 'ИС-302', '+79991112233'),
(3, 'Чарли', 'Смирнов', 'ИС-303', '+79992223344');