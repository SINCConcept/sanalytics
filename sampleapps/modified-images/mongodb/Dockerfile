FROM mongo
MAINTAINER Christian Proinger <cproinger@mailinator.com>

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY mongodb_exporter-linux-amd64 /bin/mongo_exporter

RUN apt-get update && apt-get install -y supervisor && mkdir -p /var/log/supervisor


EXPOSE 27017 9001
CMD ["/usr/bin/supervisord"]