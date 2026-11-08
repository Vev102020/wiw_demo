-- Добавляем роль в таблицу пользователей с защитой по умолчанию
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER' NOT NULL;

-- Добавляем новые колонки в УЖЕ СУЩЕСТВУЮЩУЮ таблицу профилей
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS username VARCHAR(50) UNIQUE;
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS settings TEXT DEFAULT '{}';