# ═══════════════════════════════════════════
#  Stage 1: Build (Maven + JDK 21)
# ═══════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Cache Maven dependencies trước (chỉ rebuild khi pom.xml thay đổi)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source code và build
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ═══════════════════════════════════════════
#  Stage 2: Run (JRE only — nhẹ hơn)
# ═══════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR từ stage build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
