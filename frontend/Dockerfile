# Etapa 1: Construcción de los archivos estáticos de Vue
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Etapa 2: Servidor Nginx para exponer la app
FROM nginx:alpine
# Copiamos el resultado de la compilación (la carpeta 'dist') a Nginx
COPY --from=build /app/dist /usr/share/nginx/html
# Copiamos la configuración personalizada para evitar errores 404 al recargar la página
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]