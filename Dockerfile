# Etapa 1: Build nativo con GraalVM y Gradle Wrapper
# Usamos una imagen de GraalVM con JDK 21 y la herramienta native-image disponible
FROM ghcr.io/graalvm/native-image-community:21 AS build

# Directorio de trabajo
WORKDIR /workspace

# Herramientas necesarias para el build (xargs)
RUN microdnf install -y findutils && microdnf clean all || true

# Copiamos primero los archivos que menos cambian para aprovechar la cache de Docker
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Dar permisos de ejecución al wrapper
RUN chmod +x gradlew

# Descarga de dependencias (mejora la cache). No ejecuta tests
# Nota: se usa --no-daemon para entornos de CI/CD
RUN ./gradlew --no-daemon dependencies || true

# Copiamos el resto del código fuente
COPY src/ src/

# Compilación nativa (Spring Boot + Kotlin + GraalVM)
# Si quieres omitir tests en el build del binario, descomenta -x test
RUN ./gradlew clean --no-daemon nativeCompile -x test

# Etapa 2: Imagen final con el binario nativo y zlib presente
FROM debian:12-slim AS runtime

# Instalar dependencias de runtime necesarias (zlib) y certificados
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       ca-certificates \
       zlib1g \
    && rm -rf /var/lib/apt/lists/*

# Puerto expuesto que coincide con el puerto configurado de Spring Boot
EXPOSE 8082

# Directorio de la app dentro de la imagen final
WORKDIR /app

# Ajustar el puerto de la aplicación en runtime para garantizar que escuche en 8082
# Esto evita posibles desajustes si application.properties no fuese tomado por el binario nativo
ENV SERVER_PORT=8082

# Copiar el binario generado por ./gradlew nativeCompile
COPY --from=build /workspace/build/native/nativeCompile/SpringKotlin /app/SpringKotlin

# Crear usuario no root y asignar permisos
RUN groupadd -r appuser && useradd -r -g appuser appuser \
    && chown -R appuser:appuser /app
USER appuser

# Comando de arranque
ENTRYPOINT ["/app/SpringKotlin"]
