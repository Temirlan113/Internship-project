FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .

RUN .gradle bootJar --nodaemon -x test


FROM eclipse-temurin:17-jre-alphine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]