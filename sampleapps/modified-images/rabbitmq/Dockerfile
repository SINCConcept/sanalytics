FROM rabbitmq:3.6.6-management
MAINTAINER Christian Proinger <cproinger@mailinator.com>

#COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
#rabbitmq_exporter-0.17.1.linux-amd64.tar.gz from https://github.com/kbudde/rabbitmq_exporter/releases
#COPY rabbitmq_exporter /bin/rabbitmq_exporter

#RUN apt-get update && apt-get install -y supervisor && mkdir -p /var/log/supervisor
#RUN rabbitmq-plugins enable --offline rabbitmq_management

#from https://github.com/deadtrickster/prometheus_rabbitmq_exporter #/usr/lib/rabbitmq/lib/rabbitmq_server-3.6.6/plugins
COPY prometheus_rabbitmq_exporter-v3.6.5.6.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.6/plugins/prometheus_rabbitmq_exporter-v3.6.5.6.ez
COPY prometheus_process_collector-1.0.0.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.6/plugins/prometheus_process_collector-1.0.0.ez
COPY prometheus-3.0.1.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.6/plugins/prometheus-3.0.1.ez

RUN rabbitmq-plugins enable --offline prometheus
RUN rabbitmq-plugins enable --offline prometheus_process_collector
RUN rabbitmq-plugins enable --offline prometheus_rabbitmq_exporter

#EXPOSE 15671 15672 4369 5671 5672 25672 9090
#CMD ["/usr/bin/supervisord"]