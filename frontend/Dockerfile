# Etapa de build do projeto Quasar
FROM node:18 AS build-stage

# Definir o diretório de trabalho
WORKDIR /app

# Copiar os arquivos de dependências
COPY package*.json ./
RUN npm install

# Copiar todos os arquivos do projeto
COPY . .

ARG VITE_BASE_URL

# Construir o projeto Quasar
RUN npm run build

# Usar imagem do nginx para servir os arquivos
FROM nginx:alpine AS production-stage

# Copiar o build do Quasar para o diretório padrão do nginx
COPY --from=build-stage /app/dist/spa /usr/share/nginx/html

# Expor a porta 80 para o nginx
EXPOSE 80

# Comando para rodar o nginx
CMD ["nginx", "-g", "daemon off;"]
