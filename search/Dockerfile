FROM openjdk:17

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 18100

ENTRYPOINT ["java", "-jar", "app.jar"]