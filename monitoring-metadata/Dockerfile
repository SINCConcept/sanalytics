FROM anapsix/alpine-java:8_server-jre
LABEL groupId="at.ac.tuwien.dsg.sanalytics.meta"
MAINTAINER Christian Proinger <cproinger@mailinator.com>
COPY ./target/monitoring-metadata.jar /home/monitoring-metadata.jar
#since i don't serve static content i probably don't need to touch it. 
#RUN sh -c 'touch /home/monitoring-metadata.jar'
ENV JAVA_OPTS="-Xmx128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.edg=file:/dev/./urandom -jar /home/monitoring-metadata.jar"]