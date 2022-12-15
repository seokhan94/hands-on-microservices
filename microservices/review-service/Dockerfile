FROM adoptopenjdk/openjdk12

EXPOSE 8080

ADD ./build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]