FROM openjdk:21
LABEL authors="egorm"

WORKDIR /app
ADD maven/catalog-service-0.0.1-SNAPSHOT.jar /app/catalog.jar
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "catalog.jar"]