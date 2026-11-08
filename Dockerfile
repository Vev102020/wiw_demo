# Сразу берем легковесный рантайм, никакой сборки внутри!
FROM eclipse-temurin:21-jre-alpine
EXPOSE 8080
WORKDIR /app

COPY server/build/libs/wiw-chat-server.jar /app/wiw-chat-server.jar
ENTRYPOINT ["java", "-jar", "/app/wiw-chat-server.jar"]


# # Сборка проекта с кэшированием зависимостей
# FROM gradle:8.7-jdk21-jammy AS build
# WORKDIR /home/gradle/src
# # Копируем конфигурационные файлы Gradle, которые определяют библиотеки
# COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts gradle.properties /home/gradle/src/
# COPY --chown=gradle:gradle shared/build.gradle.kts /home/gradle/src/shared/
# COPY --chown=gradle:gradle server/build.gradle.kts /home/gradle/src/server/
#
# # Скачиваем все зависимости в кэш Docker (этот шаг выполнится ОДИН раз)
# RUN ./gradlew :server:dependencies :shared:dependencies --no-daemon || true
# # Копируем сам исходный код (этот слой сбросит кэш только если вы изменили код)
# COPY --chown=gradle:gradle . /home/gradle/src
# # Собираем проект (библиотеки уже скачаны, сборка пройдет быстро)
# RUN ./gradlew :shared:generateBuildKonfig :server:shadowJar -x test --no-daemon
#
# # Запуск приложения
# FROM eclipse-temurin:21-jre-alpine
# EXPOSE 8080
# WORKDIR /app
#
# # Копируем готовый JAR-файл из этапа сборки
# COPY --from=build /home/gradle/src/server/build/libs/wiw-chat-server.jar /app/wiw-chat-server.jar
# ENTRYPOINT ["java", "-jar", "/app/wiw-chat-server.jar"]

