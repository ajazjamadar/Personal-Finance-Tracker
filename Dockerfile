FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -Dmaven.test.skip=true


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:prod}
ENTRYPOINT ["java", "-jar", "app.jar"]