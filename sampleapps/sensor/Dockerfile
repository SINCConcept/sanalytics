FROM openjdk:8-jre
LABEL groupId="at.ac.tuwien.dsg.sanalytics.samples"
MAINTAINER Christian Proinger <cproinger@mailinator.com>
COPY ./target/sample-sensor.jar /home/sample-sensor.jar
CMD ["java", "-Dmqtt.brokerURL=tcp://mqtt:1883", "-jar", "/home/sample-sensor.jar"]