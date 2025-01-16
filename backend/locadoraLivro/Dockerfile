FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

COPY backend/locadoraLivro/pom.xml ./
RUN mvn dependency:go-offline

COPY backend/locadoraLivro/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8040

CMD ["java", "-jar", "app.jar"]