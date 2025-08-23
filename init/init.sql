CREATE USER auth_user WITH PASSWORD 'secret';
CREATE USER user_user WITH PASSWORD 'secret';

CREATE DATABASE auth_db OWNER auth_user;
CREATE DATABASE user_db OWNER user_user;

GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;
GRANT ALL PRIVILEGES ON DATABASE user_db TO user_user;