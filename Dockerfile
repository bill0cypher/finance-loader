FROM openjdk:15-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} finance-loader.jar
ENTRYPOINT ["java", "-jar", "/finance-loader.jar"]