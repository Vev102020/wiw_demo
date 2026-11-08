package core.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            // Игнорировать неизвестные поля в запросах от клиента.
            // Если клиент пришлет { "id": 1, "new_feature_field": "test" },
            // а сервер об этом поле еще не знает — он не упадет, а просто проигнорирует его.
            ignoreUnknownKeys = true

            // Включать в JSON-ответ только измененные поля.
            // Если у класса есть значение по умолчанию (например, avatarUrl: String? = null),
            // сервер НЕ будет сериализовать и слать строчку "avatarUrl": null.
            // Это экономит до 30% сетевого трафика.
            encodeDefaults = false

            // Строгий режим проверки типов.
            // Запретит превращать пустые строки "" в null-объекты, защищая от ошибок в БД.
            isLenient = false

            // Красивый вывод JSON в логах.
            // true — делает JSON читаемым (с отступами и переносами строк). Useful для локальной разработки.
            // Для релизного продакшена лучше ставить false, чтобы сжимать размер текста.
            prettyPrint = true
        })
    }
}