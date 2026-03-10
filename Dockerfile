# I am using a lightweight java 17/21 alpine image
FROM eclipse-temurin:21-jdk-alpine

# Setting up the working directory inside the container
WORKDIR /app

# Copy the compiled jar file into the container
COPY target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]