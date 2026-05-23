# Etapa 1 — Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom y descargar dependencias primero (cache de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2 — Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar JAR del build
COPY --from=build /app/target/recolectaDemo-0.0.1-SNAPSHOT.jar app.jar

# Puerto
EXPOSE 8080

# Arrancar con perfil inyectable por variable de entorno
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILE:dev}", "app.jar"]