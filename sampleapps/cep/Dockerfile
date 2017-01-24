FROM openjdk:8-jre
LABEL groupId="at.ac.tuwien.dsg.sanalytics.samples"
MAINTAINER Christian Proinger <cproinger@mailinator.com>
COPY ./target/sample-cep.jar /home/sample-cep.jar
CMD ["java", "-Dspring.data.mongodb.uri=mongodb://mymongodb:27017/events", "-Dmqtt.brokerURL=tcp://mqtt:1883", "-jar", "/home/sample-cep.jar"]