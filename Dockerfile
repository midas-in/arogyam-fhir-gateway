FROM docker.io/library/maven:3.9.6-eclipse-temurin-17 AS build-fhir-gateway
WORKDIR /tmp/fhir-gateway

ARG OPENTELEMETRY_JAVA_AGENT_VERSION=1.33.3
RUN curl -LSsO https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OPENTELEMETRY_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar

COPY pom.xml .
RUN mvn -ntp dependency:go-offline

COPY src/ /tmp/fhir-gateway/src/
RUN mvn clean install -DskipTests -Djdk.lang.Process.launchMechanism=vfork

FROM build-fhir-gateway AS build-distroless
RUN mvn package -DskipTests spring-boot:repackage
RUN mkdir /app && cp /tmp/fhir-gateway/target/ROOT.war /app/main.war

########### distroless brings focus on security and runs on plain spring boot - this is the default image
FROM gcr.io/distroless/java17-debian12:nonroot AS default
# 65532 is the nonroot user's uid
# used here instead of the name to allow Kubernetes to easily detect that the container
# is running as a non-root (uid != 0) user.
USER 65532:65532
WORKDIR /app

COPY --chown=nonroot:nonroot --from=build-distroless /app /app
COPY --chown=nonroot:nonroot --from=build-fhir-gateway /tmp/fhir-gateway/opentelemetry-javaagent.jar /app

ENTRYPOINT ["java", "--class-path", "/app/main.war", "-Dloader.path=main.war!/WEB-INF/classes/,main.war!/WEB-INF/,/app/extra-classes", "org.springframework.boot.loader.launch.PropertiesLauncher"]