FROM eclipse-temurin:21-jre-alpine
EXPOSE 8080
WORKDIR /app

COPY server/build/libs/wiw-chat-server.jar /app/wiw-chat-server.jar
ENTRYPOINT ["java", "-jar", "/app/wiw-chat-server.jar"]
