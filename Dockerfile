FROM openjdk:8-jdk-alpine
MAINTAINER mani
WORKDIR /
ADD calendly-application-1.0.0-snapshot.jar calendly-application-1.0.0-snapshot.jar
EXPOSE 7002
CMD ["java", "-jar", "calendly-application-1.0.0-snapshot.jar"]
