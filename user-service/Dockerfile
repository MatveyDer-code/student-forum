FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY user-service/src ./src
COPY user-service/build.gradle.kts ./
COPY gradlew .
COPY gradle gradle

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]