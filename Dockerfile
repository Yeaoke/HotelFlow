FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Maven Wrapper и конфигурация Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Разрешение на запуск Maven Wrapper
RUN chmod +x mvnw

# Загрузка зависимостей
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Сборка приложения
RUN ./mvnw clean package -DskipTests -B


# Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]