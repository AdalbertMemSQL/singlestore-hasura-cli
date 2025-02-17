FROM registry.access.redhat.com/ubi9/openjdk-17:1.18-4

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

WORKDIR /app
COPY . /app

# Run Gradle build
USER root
RUN ./gradlew :ndc-cli-singlestore:installDist --no-daemon --console=plain -x test

ENTRYPOINT ["/app/ndc-cli-singlestore/build/install/ndc-cli-singlestore/bin/ndc-cli-singlestore"]
