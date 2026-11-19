# Recipe the hosting site uses to build and run the server. You never need to run Docker yourself.

# 1) Build the server jar with Maven
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q clean package -DskipTests

# 2) Run it with just a Java runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/quizmaster-server.jar app.jar
ENV APP_ENV=prod \
    DB_PATH=/data/quiz.db \
    PORT=8080
EXPOSE 8080
CMD ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
