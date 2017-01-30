FROM anapsix/alpine-java:8_server-jre
MAINTAINER Christian Proinger <cproinger@mailinator.com>
COPY ./target/filter-proxy-standalone.war /home/filter-proxy-standalone.war
#CMD ["java", "-jar", "/home/filter-proxy-standalone.war"]
# this is taken from https://spring.io/guides/gs/spring-boot-docker/
#since i don't serve static content i probably don't need to touch it. 
#RUN sh -c 'touch /home/filter-proxy-standalone.war'
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.edg=file:/dev/./urandom -jar /home/filter-proxy-standalone.war"]