# Usar a imagem base do Eclipse Temurin
FROM eclipse-temurin:17-jdk-jammy

# Definir o diretório de trabalho
WORKDIR /app

# Variável para o nome do arquivo JAR
ARG JAR_FILE=target/SistemaNutricao-0.0.1-SNAPSHOT.jar

# 1. Copiar o JAR mantendo o nome consistente
COPY ${JAR_FILE} app.jar

COPY src/main/resources/bootstrap/Acidos.xlsx src/main/resources/bootstrap/Taco.xlsx ./bootstrap/

# 2. Verificar integridade do JAR
RUN apt-get update && \
    apt-get install -y file && \
    file app.jar | grep 'Java archive' && \
    jar tf app.jar > /dev/null && \
    jar tf app.jar | grep -q 'BOOT-INF/classes/templates/pages/general/Home.html' && \
    echo "Verificação do JAR bem-sucedida" || (echo "ERRO: JAR corrompido ou incompleto" && exit 1)

# 3. Criar diretório para uploads com permissões adequadas
RUN mkdir -p /app/uploads && \
    chmod -R 775 /app/uploads && \
    chown -R 1000:1000 /app/uploads

# 4. Configurar usuário não-root
RUN useradd -ms /bin/bash springuser && \
    chown -R springuser:springuser /app
USER springuser

# Expor a porta 8080
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]