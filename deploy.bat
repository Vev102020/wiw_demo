@echo off
call gradlew.bat :server:shadowJar
if %errorlevel% neq 0 exit /b %errorlevel%
docker compose up --build -d wiw-chat-server