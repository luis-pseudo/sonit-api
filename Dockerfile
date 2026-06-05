# ── Stage 1: Build ──────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
# Descarga dependencias primero (cache layer)
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-bookworm

WORKDIR /app

# Usuario no-root por seguridad
RUN groupadd -r sonit && useradd -r -g sonit sonit

COPY --from=builder /app/target/*.jar app.jar

RUN chown sonit:sonit app.jar
USER sonit

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
