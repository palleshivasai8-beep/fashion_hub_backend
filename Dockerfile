# Multi-stage Docker build for Spring Boot on Render
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and POM first for better layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x ./mvnw

# Pre-fetch project dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy project source and package the Spring Boot executable JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Lightweight JRE runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built Spring Boot fat JAR from the builder stage
COPY --from=build /app/target/FashionHub-0.0.1-SNAPSHOT.jar app.jar

# Render automatically provides the PORT environment variable (defaults to 8081 locally)
ENV PORT=8081
EXPOSE 8081

# Optimize JVM startup memory and random seed generation for cloud containers
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -jar app.jar"]
