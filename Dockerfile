# ------------ BUILD STAGE ------------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src

# Build fat jar with preview enabled
RUN mvn -q -DskipTests package

# ------------ RUNTIME STAGE ------------
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/app.jar app.jar

# Expose the port
EXPOSE 9090

# Enable Java preview features at runtime
ENV JAVA_OPTS="--enable-preview"

# Run the fat jar
CMD ["sh", "-c", "cp /etc/secrets/* /app/ && java $JAVA_OPTS -jar app.jar"]
 