FROM openjdk:11-jre-slim
RUN apt-get update &&  \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*
RUN mkdir worker
COPY target/worker-1.0-SNAPSHOT.jar aplikacija.jar
RUN useradd -u 1234 non-root
RUN chown non-root /worker
USER non-root
WORKDIR /worker
ENTRYPOINT ["java", "-jar", "../aplikacija.jar"]