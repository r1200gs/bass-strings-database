FROM eclipse-temurin:25-alpine

# Create a non-root system user for security purposes
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy the compiled heavy JAR file from the builder stage
# Replace 'your-app-name' with your actual artifactId from pom.xml
COPY ./target/*.jar /app/app.jar

# Expose the application port (adjust to match your application configuration)
EXPOSE 8080

# Run the compiled application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
