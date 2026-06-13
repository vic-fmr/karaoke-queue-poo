# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app

# Instala dependências
COPY package*.json ./
RUN npm install

# Build da aplicação
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Serve com Nginx
FROM nginx:stable-alpine

# Copia os arquivos estáticos (ajuste o caminho se o nome do projeto no angular.json for diferente)
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copia configuração customizada do Nginx para suportar rotas SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
