# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage - Optimized for 512MB RAM
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run with memory optimization flags for 512MB container
# -Xms128m: Initial heap size
# -Xmx256m: Maximum heap size
# -XX:MaxMetaspaceSize=128m: Limit metaspace
# -XX:+UseG1GC: Use G1 garbage collector for better memory efficiency
# -XX:MaxGCPauseMillis=200: Target max GC pause time
# -XX:+UseContainerSupport: Enable container-aware JVM
ENTRYPOINT ["java", \
    "-Xms128m", \
    "-Xmx256m", \
    "-XX:MaxMetaspaceSize=128m", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+UseContainerSupport", \
    "-jar", \
    "app.jar"]
