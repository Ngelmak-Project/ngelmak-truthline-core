# --- Build Stage ---
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven descriptor and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package

# --- Runtime Stage ---
FROM openjdk:21-jdk AS runner

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder ./app/target/*-SNAPSHOT.jar ./app.jar

# Expose service port
EXPOSE 4005

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]