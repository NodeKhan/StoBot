# ---- Build stage (local dev) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -DskipTests clean package

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# En local : vient du stage build
# En CI : le fichier target/*.jar est copié dans le context, écrase le stage build
COPY --from=build /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
CMD ["java", "-jar", "app.jar"]
