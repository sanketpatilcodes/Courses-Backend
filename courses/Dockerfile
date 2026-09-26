FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace
COPY . .

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon \
    && find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -exec cp {} /tmp/app.jar \;

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /tmp/app.jar app.jar
COPY --from=build /workspace/content ./content

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080}"]