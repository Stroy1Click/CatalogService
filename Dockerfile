FROM eclipse-temurin:21-jre-alpine
LABEL authors="egorm"

WORKDIR /app
COPY target/catalog-service-0.0.1-SNAPSHOT.jar /app/catalog.jar
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "catalog.jar"]