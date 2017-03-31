FROM anapsix/alpine-java:8
LABEL groupId="at.ac.tuwien.dsg.sanalytics.samples"
MAINTAINER Christian Proinger <cproinger@mailinator.com>
COPY ./target/sample-sensor.jar /home/sample-sensor.jar
ENV JAVA_OPTS=""
#CMD ["java", "-Dmqtt.brokerURL=tcp://mqtt:1883", "-jar", "/home/sample-sensor.jar"]
VOLUME /data
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /home/sample-sensor.jar"]