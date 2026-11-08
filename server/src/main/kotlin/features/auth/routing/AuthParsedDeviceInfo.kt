package features.auth.routing

import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import ua_parser.Parser

private val uaParser: Parser by lazy { Parser() }

fun parseUserAgentSafely(rawUserAgent: String?): String {
    if (rawUserAgent.isNullOrBlank()) return AuthConstants.DEFAULT_DEVICE_INFO

    return try {
        // Метод parse() возвращает объект ua_parser.Client
        val client = uaParser.parse(rawUserAgent)

        // Поля в Java-классах библиотеки uap-java:
        // client.userAgent -> объект класса UserAgent (поля: family, major, minor, patch)
        // client.os        -> объект класса OS (поля: family, major, minor, patch, patchMinor)
        val browserName = client.userAgent?.family // Например: "Chrome", "Firefox"
        val osName = client.os?.family             // Например: "Windows", "Android", "iOS"
        val osMajorVersion = client.os?.major       // Например: "11", "14", "17"

        if (osName != null && osName != "Other") {
            val osDetails = if (osMajorVersion != null) "$osName $osMajorVersion" else osName
            "$browserName ($osDetails)"
        } else {
            browserName ?: rawUserAgent.take(50).trim()
        }
    } catch (e: Exception) {
        // В случае любой непредвиденной аномалии возвращаем безопасный дефолт
        AuthConstants.DEFAULT_DEVICE_INFO
    }
}