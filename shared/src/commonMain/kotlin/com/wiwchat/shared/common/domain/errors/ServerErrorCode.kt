package com.wiwchat.shared.common.domain.errors

enum class ServerErrorCode : ApiErrorCode {
    // Ошибки запроса и валидации (HTTP 400 Bad Request)
    VALIDATION_ERROR,           // Неверный формат данных (длина, спецсимволы в тегах)
    BAD_REQUEST_BODY,           // Сломанный JSON (заменил ваш INVALID_REQUEST_FORMAT)
    MISSING_REQUIRED_PARAMS,    // Нехватка обязательных полей в JSON (MissingFieldException)
    INVALID_IDENTIFIER_FORMAT,  // Неверный UUID/Long формат (заменил SERVER_INVALID_IDENTIFIER_FORMAT)

    // Безопасность и доступ (HTTP 401 / 403)
    UNAUTHORIZED,               // Токен отсутствует, подделан или просрочен
    INVALID_CREDENTIALS,        // Неверный логин или пароль при входе
    ACCESS_DENIED,              // Недостаточно прав (попытка обычного юзера зайти в админку)
    ACCOUNT_BANNED,             // Аккаунт заблокирован глобально

    // Ресурсы и Конфликты (HTTP 404 / 409)
    ROUTE_NOT_FOUND,            // Неверный URL эндпоинта (заменил SERVER_ROUTE_NOT_FOUND)
    RESOURCE_NOT_FOUND,         // Чат, сообщение или юзер не найдены в базе данных
    RESOURCE_ALREADY_EXISTS,    // Конфликт уникальности (например, дубликат email)
    EDITING_TIME_EXPIRED,       // Прошло больше 48 часов для редактирования сообщения

    // ГРУППА 4: Лимиты и Спам (HTTP 429 Too Many Requests)
    RATE_LIMIT_EXCEEDED,        // Слишком частые запросы (заменил SERVER_TOO_MANY_REQUESTS)
    STORAGE_LIMIT_EXCEEDED,     // Исчерпан лимит облачного хранилища секретных паролей

    // Критические сбои сервера (HTTP 500)
    SERVER_UNKNOWN_ERROR,       // Неопознанный системный сбой
    SERVER_INTERNAL_ERROR;      // Сбой базы данных, NullPointerException и т.д.

    companion object {
        fun find(name: String): ApiErrorCode? = entries.find { it.name == name }
    }
}
