# ---- Estagio de build: compila e empacota o jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa dependencias primeiro (camada cacheavel)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Compila e empacota (testes rodam no pipeline; aqui pulamos para acelerar a imagem)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Estagio de runtime: imagem enxuta apenas com a JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/banco-digital-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
