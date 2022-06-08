FROM openjdk:11-jre-slim
RUN apt-get update &&  \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*
COPY target/worker-1.0-SNAPSHOT.jar aplikacija.jar
ENTRYPOINT ["java", "-jar", "aplikacija.jar"]