# Use a lightweight Java runtime image
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from your target folder into the container
# The wildcard (*) ensures it grabs the JAR regardless of the exact version number
COPY target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]