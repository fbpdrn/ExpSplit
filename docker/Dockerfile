# Il modulo da costruire è selezionato tramite MODULE (vedi docker-compose.yml).

FROM eclipse-temurin:25-jdk AS build
ARG MODULE
WORKDIR /workspace
COPY . .
RUN ./gradlew :${MODULE}:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre AS runtime
ARG MODULE
WORKDIR /app
COPY --from=build /workspace/${MODULE}/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
