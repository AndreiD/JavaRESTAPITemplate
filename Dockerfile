FROM openjdk:10-jre
ADD ./target/restapi-exec.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/restapi-exec.jar"]

EXPOSE 8080