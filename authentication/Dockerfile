FROM openjdk:17

WORKDIR /app

COPY target/*.jar app.jar
COPY src/main/resources/admin-emails.properties /app/admin-emails.properties


EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]