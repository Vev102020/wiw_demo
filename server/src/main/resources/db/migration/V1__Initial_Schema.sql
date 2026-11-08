-- Включаем поддержку генерации UUID на стороне PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Таблица пользователей (только авторизационные данные)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица профилей (связь 1:1 с пользователем)
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(100),
    avatar_url TEXT,
    status VARCHAR(20) DEFAULT 'OFFLINE', -- ONLINE, OFFLINE, AWAY
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    app_settings JSONB DEFAULT '{}'::jsonb -- Тема, уведомления и т.д.
);

-- Таблица сессий для JWT и FORCE_LOGOUT. Устройства
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token TEXT NOT NULL UNIQUE,
    is_banned BOOLEAN DEFAULT FALSE,
    device_info TEXT, -- Например: "Android SDK 34", "Chrome Browser"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
