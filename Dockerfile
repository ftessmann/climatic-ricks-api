FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY . .

RUN mvn -B -DskipTests clean package

CMD ["java", "-jar", "target/quarkus-app/quarkus-run.jar"]
