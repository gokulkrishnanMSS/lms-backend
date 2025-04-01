# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app
# Copy the jar file built by Spring Boot to the container
COPY target/lms-0.0.1-SNAPSHOT.jar /app/lms-0.0.1-SNAPSHOT.jar
COPY app.env /app/.env
COPY app.ini /app/app.ini

# Expose the port your app will run on
EXPOSE 8080

# Command to run your app
ENTRYPOINT ["java", "-jar", "/app/lms-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=dev"]
