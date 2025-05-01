FROM maven:3.9-amazoncorretto-17 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine

WORKDIR /app
COPY --from=builder /app/target/getscheme-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=production"] 