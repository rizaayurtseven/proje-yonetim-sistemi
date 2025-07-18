# Java 21 ve Maven 3.9.6 içeren base image
FROM maven:3.9.6-eclipse-temurin-21 as build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Uygulama çalıştırma aşaması
FROM eclipse-temurin:21
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
