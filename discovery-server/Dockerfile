# Sử dụng image OpenJDK 21 slim làm base image
FROM openjdk:21-jdk

# Đặt thư mục làm việc trong container
WORKDIR /app

# Sao chép file JAR từ máy host vào container
COPY target/discovery-server-0.0.1-SNAPSHOT.jar app.jar


# Lệnh chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "/app/app.jar"]