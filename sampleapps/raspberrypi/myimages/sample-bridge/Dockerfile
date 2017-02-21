FROM armhf/openjdk:openjdk-8-jre
LABEL groupId="at.ac.tuwien.dsg.sanalytics.samples"
MAINTAINER Christian Proinger <cproinger@mailinator.com>
COPY ./sample-bridge.jar /home/sample-bridge.jar
#since i don't serve static content i probably don't need to touch it. 
#RUN sh -c 'touch /home/sample-bridge.jar'
ENV JAVA_OPTS="-Xmx128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.edg=file:/dev/./urandom -jar /home/sample-bridge.jar"]