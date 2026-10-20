FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

COPY src src

RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && useradd --system --uid 10001 app

WORKDIR /app

COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
