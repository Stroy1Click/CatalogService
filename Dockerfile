FROM openjdk:21
LABEL authors="egorm"

WORKDIR /app
COPY target/Stroy1Click-ProductService-0.0.1-SNAPSHOT.jar /app/product.jar
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "product.jar"]