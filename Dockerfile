# Этап сборки
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle shadowJar --no-daemon

# Этап запуска
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем собранный JAR из предыдущего этапа
COPY --from=builder /app/build/libs/*.jar app.jar

# Указываем команду запуска
CMD ["java", "-jar", "app.jar"]
