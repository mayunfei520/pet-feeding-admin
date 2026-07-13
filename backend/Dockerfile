FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/pet-feeding-platform-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
