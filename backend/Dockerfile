# --- Étape 1 : Build (Node.js) ---
FROM node:18-alpine AS build
WORKDIR /app

# Installation des dépendances
COPY package.json package-lock.json ./
RUN npm install

# Copie du code et build
COPY ../frontend .
RUN npm run build

# --- Étape 2 : Serve (Nginx) ---
FROM nginx:alpine

# On copie le dossier "dist" (ou "build" selon ton projet React) vers Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# (Optionnel) Config Nginx pour React Router (évite les erreurs 404 au refresh)
# COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]