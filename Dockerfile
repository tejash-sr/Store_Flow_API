FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
ARG JAR_FILE=target/storeflow-api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]