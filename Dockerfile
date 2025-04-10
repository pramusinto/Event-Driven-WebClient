FROM 10.70.141.12:80/library/eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Create a directory to store the certificates
RUN mkdir -p /etc/ssl/private/

# Copy the certificate files into the image
COPY ca.crt /etc/ssl/private/
COPY tls.key /etc/ssl/private/

COPY target/*.jar /app/service.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=default

# Set the entry point for the container
ENTRYPOINT ["/bin/sh", "/app/entrypoint.sh"]


