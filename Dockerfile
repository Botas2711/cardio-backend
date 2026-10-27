# Etapa 1: Compilación de la aplicación con Gradle y JDK 17
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos de configuración de Gradle para aprovechar la caché de capas Docker
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN touch gradle.properties
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copiar el código fuente y compilar el JAR ejecutable excluyendo tests para acelerar el despliegue
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Etapa 2: Imagen final liviana de ejecución con JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
