-- Делаем display_name будет null
ALTER TABLE user_profiles ALTER COLUMN display_name DROP NOT NULL;