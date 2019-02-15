# package app with openjdk jre (see https://hub.docker.com/_/openjdk/)
FROM openjdk:11.0.2-jre-slim
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
VOLUME /tmp
COPY target/manon.jar app.jar
ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-Dspring.jmx.enabled=false","-Dspring.profiles.active=docker","-jar","/app.jar"]
