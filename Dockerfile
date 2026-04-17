# ----------- BUILD STAGE -----------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy only required files first (better caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Give permission to mvnw
RUN chmod +x mvnw

# Download dependencies (faster rebuilds)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build jar
RUN ./mvnw clean package -DskipTests

# ----------- RUN STAGE -----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Set profile
ENV SPRING_PROFILES_ACTIVE=prod

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
