# Base image: Java 21 JRE (nhẹ, chuẩn)
FROM eclipse-temurin:21-jre

# Thư mục chạy app
WORKDIR /app

# Copy jar vào container
COPY target/history-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
