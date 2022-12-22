FROM adoptopenjdk/openjdk12

EXPOSE 8761

ADD ./build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]