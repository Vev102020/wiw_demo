package com.wiwchat.shared.core.config

import com.wiwchat.shared.config.BuildKonfig

object AppEnvironment {
    // Маскируем домены, разбивая их или собирая динамически,
    // чтобы их нельзя было достать простым поиском строк в apk/ios-бинарнике
    private val PROD_PARTS = listOf("https://", "wiwchat", ".com")
    private const val LOCAL_HOST = "http://10.0.2.2:8080" // Дефолтный локалхост для эмулятора Android

    val baseUrl: String
        get() {
            // Если мы собираем релиз (для прода) — собираем боевой домен
            if (BuildKonfig.IS_RELEASE) {
                return PROD_PARTS.joinToString("")
            }

            // Если это дебаг-сборка — смотрим, подставил ли разработчик локальный IP.
            // На дебаге мы можем позволить себе читать системные свойства или возвращать локалхост
            return LOCAL_HOST
        }
}