FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiamos TODO el repo dentro del contenedor
COPY . /app

# Entramos en el subproyecto donde está el Spring Boot
WORKDIR /app/tennis-club-manager

# Dar permisos al wrapper de Maven
RUN chmod +x mvnw

# Construir el JAR
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

# Arrancar la app (usamos comodín por si cambia el nombre del jar)
CMD ["sh", "-c", "java -jar target/*.jar"]