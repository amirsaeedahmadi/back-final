FROM openjdk:17

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8095

ENTRYPOINT ["java", "-jar", "app.jar"]