FROM anapsix/alpine-java:8_server-jre
LABEL groupId="at.ac.tuwien.dsg.sanalytics.samples"
MAINTAINER Christian Proinger <cproinger@mailinator.com>
COPY ./target/sample-cep.jar /home/sample-cep.jar
#CMD ["java", "", "-jar", "/home/sample-cep.jar"]
#since i don't serve static content i probably don't need to touch it. 
#RUN sh -c 'touch /home/sample-cep.jar'
ENV JAVA_OPTS="-Xmx64m -Dspring.data.mongodb.uri=mongodb://mymongodb:27017/events -Dmqtt.brokerURL=tcp://mqtt:1883"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.edg=file:/dev/./urandom -jar /home/sample-cep.jar"]