-- Создание перечислений (Enums) для новых сущностей
CREATE TYPE chat_room_type AS ENUM ('DIRECT', 'GROUP', 'SAVED');
CREATE TYPE voice_room_type AS ENUM ('PERMANENT', 'TEMPORARY');
CREATE TYPE tag_color_palette AS ENUM ('WHITE', 'RED', 'BLUE', 'GREEN', 'ORANGE', 'PURPLE');
CREATE TYPE edit_message_severity AS ENUM ('TEXT_MINOR', 'TEXT_MAJOR', 'MEDIA_MAJOR', 'CRITICAL_SWAP');
CREATE TYPE relation_type AS ENUM ('MUTE_SOUND', 'HIDE_MESSAGES', 'GLOBAL_BLOCK');
CREATE TYPE chat_role AS ENUM ('CREATOR', 'ADMIN', 'MODERATOR', 'MEMBER');

-- Таблица комнат/комнат чатов
CREATE TABLE IF NOT EXISTS chat_rooms (
    id UUID PRIMARY KEY,
    type chat_room_type NOT NULL,
    title VARCHAR(150),
    avatar_url TEXT,
    owner_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE messages ADD CONSTRAINT fk_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE;

-- Таблица участников чата
CREATE TABLE IF NOT EXISTS chat_participants (
    room_id UUID REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_in_chat chat_role DEFAULT 'MEMBER' NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_muted BOOLEAN DEFAULT FALSE NOT NULL,
    muted_until TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (room_id, user_id)
);

-- Таблица архива
CREATE TABLE IF NOT EXISTS archived_chats (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    room_id UUID REFERENCES chat_rooms(id) ON DELETE CASCADE,
    archived_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, room_id)
);

-- Таблица истории изменений сообщений
CREATE TABLE IF NOT EXISTS message_history (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL,
    message_sequence_id BIGINT NOT NULL,
    old_text TEXT NOT NULL,
    severity edit_message_severity NOT NULL,
    edited_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (room_id, message_sequence_id) REFERENCES messages(room_id, message_sequence_id) ON DELETE CASCADE
);

-- Таблица тегов с Enum-палитрой
CREATE TABLE IF NOT EXISTS message_tags (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(30) NOT NULL,
    color tag_color_palette DEFAULT 'WHITE' NOT NULL,
    is_secret BOOLEAN DEFAULT FALSE NOT NULL,
    is_editable BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Связующая таблица тегов с сообщениями по составному ключу
CREATE TABLE IF NOT EXISTS message_tag_relations (
    room_id UUID NOT NULL,
    message_sequence_id BIGINT NOT NULL,
    tag_id UUID REFERENCES message_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (room_id, message_sequence_id, tag_id),
    FOREIGN KEY (room_id, message_sequence_id) REFERENCES messages(room_id, message_sequence_id) ON DELETE CASCADE
);

-- Таблица голосовых каналов
CREATE TABLE IF NOT EXISTS voice_channels (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    type voice_room_type DEFAULT 'PERMANENT' NOT NULL,
    creator_id UUID REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Универсальная таблица приватности и связей пользователей
CREATE TABLE IF NOT EXISTS user_relations (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    target_user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    relation_type relation_type NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, target_user_id)
);

-- Таблица Избранного
CREATE TABLE IF NOT EXISTS saved_messages (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    original_room_id UUID NOT NULL,
    original_message_sequence_id BIGINT NOT NULL,
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (original_room_id, original_message_sequence_id) REFERENCES messages(room_id, message_sequence_id) ON DELETE CASCADE
);

-- Добавление индексов для ускорения поиска по тегам и комнатам
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_expiry ON messages(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_messages_parent_thread ON messages(room_id, parent_message_sequence_id) WHERE parent_message_sequence_id IS NOT NULL;
CREATE INDEX idx_message_tags_user ON message_tags(user_id);
CREATE INDEX idx_user_relations_lookup ON user_relations(user_id, relation_type);
CREATE INDEX idx_archived_chats_lookup ON archived_chats(user_id);
CREATE INDEX idx_saved_messages_user ON saved_messages(user_id);