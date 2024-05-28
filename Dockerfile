FROM openjdk:17
COPY target/risephonebook*.jar /usr/src/risephonebook.jar
COPY src/main/resources/application.properties /opt/conf/application.properties
CMD ["java", "-jar", "/usr/src/risephonebook.jar", "--spring.config.location=file:/opt/conf/application.properties"]