FROM openjdk:22
ARG JAR_FILE=out/artifacts/coding_project_jar/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]