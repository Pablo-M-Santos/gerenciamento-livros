<<<<<<< HEAD

FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

COPY locadoraLivro/pom.xml ./
=======
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

COPY locadoraLivro/pom.xml ./ 
>>>>>>> 6328702c3b85d608e030f9b2e7640b556fc88434
RUN mvn dependency:go-offline

COPY locadoraLivro/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8040

<<<<<<< HEAD
ENTRYPOINT ["java", "-jar", "app.jar"]
=======
ENTRYPOINT ["java", "-jar", "app.jar"]
>>>>>>> 6328702c3b85d608e030f9b2e7640b556fc88434
