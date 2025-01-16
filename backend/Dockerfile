# Etapa de construção
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar o arquivo pom.xml e baixar as dependências do Maven
COPY locadoraLivro/pom.xml ./
RUN mvn dependency:go-offline -B

# Copiar o código-fonte do projeto
COPY locadoraLivro ./locadoraLivro
WORKDIR /app/locadoraLivro

# Construir o pacote do projeto
RUN mvn clean package -DskipTests

# Etapa de execução
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar o arquivo .jar gerado para o contêiner
COPY --from=build /app/locadoraLivro/target/*.jar app.jar

# Expor a porta que a aplicação utiliza
EXPOSE 8040

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
