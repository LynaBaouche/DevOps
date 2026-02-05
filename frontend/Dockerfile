FROM nginx:alpine
COPY . /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
COPY nginx.conf /etc/nginx/conf.d/default.conf

