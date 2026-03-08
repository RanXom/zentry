# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Download dependencies first (Heavy Layer - Cached)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build (Light Layer - Frequent changes)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S sentinel && adduser -S sentinel -G sentinel
# Ensure the user has permissions to the app directory if it needs to write logs
RUN chown -R sentinel:sentinel /app

USER sentinel

COPY --from=build /app/target/*.jar app.jar

# Use /dev/urandom for faster entropy/startup in containers (tooning hehe)
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

EXPOSE 8080