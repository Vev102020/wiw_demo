CREATE TYPE message_type AS ENUM ('TEXT', 'IMAGE', 'FILE', 'VOICE', 'SYSTEM', 'SECRET');

-- Добавляем Enum-статус аккаунта и таймстампы из доменной модели
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_status VARCHAR(30) DEFAULT 'PENDING_VERIFICATION' NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMP DEFAULT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP DEFAULT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Удаляем старую колонку create_at (если Exposed использует стандартное имя created_at)
-- Чтобы не потерять данные, переносим их, если это необходимо
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='create_at') THEN
        ALTER TABLE users RENAME COLUMN create_at TO created_at;
    END IF;
END $$;

-- Добавляем поля для поддержки голосовых комнат, модерации звука и кастомных статус-текстов
ALTER TABLE user_profiles ADD COLUMN custom_status_text VARCHAR(150) DEFAULT NULL;
ALTER TABLE user_profiles ADD COLUMN current_voice_channel_id UUID DEFAULT NULL;
ALTER TABLE user_profiles ADD COLUMN is_muted BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE user_profiles ADD COLUMN is_deafened BOOLEAN DEFAULT FALSE NOT NULL;


CREATE TABLE IF NOT EXISTS messages (
    room_id UUID NOT NULL,
    message_sequence_id BIGINT NOT NULL,
    sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
    text TEXT,
    type message_type DEFAULT 'TEXT' NOT NULL,
    is_encrypted BOOLEAN DEFAULT FALSE NOT NULL,
    parent_message_sequence_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (room_id, message_sequence_id),
    FOREIGN KEY (room_id, parent_message_sequence_id) REFERENCES messages(room_id, message_sequence_id) ON DELETE CASCADE
);
