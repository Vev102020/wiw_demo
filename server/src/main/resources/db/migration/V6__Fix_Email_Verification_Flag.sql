-- Удаляем избыточную булеву колонку
ALTER TABLE users DROP COLUMN IF EXISTS is_email_verified;
