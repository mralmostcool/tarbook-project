FROM nginx:alpine

# Remove default configuration
RUN rm -rf /etc/nginx/conf.d/default.conf

# Copy custom configurations
COPY nginx.conf /etc/nginx/nginx.conf
COPY conf.d/ /etc/nginx/conf.d/

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
