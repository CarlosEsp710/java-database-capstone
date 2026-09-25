FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17.0.15_6-jre

WORKDIR /app
RUN useradd --system --uid 10001 --home-dir /app clinic
COPY --from=builder --chown=10001:10001 /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar
USER 10001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
