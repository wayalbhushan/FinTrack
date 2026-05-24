# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/manager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

# Dynamic database URL translation at runtime
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=prod -Dspring.datasource.url=$(echo ${DATABASE_URL} | sed 's/postgres:/jdbc:postgresql:/') -jar app.jar"]
