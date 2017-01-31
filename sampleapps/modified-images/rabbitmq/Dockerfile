FROM rabbitmq:3.6.6-management
MAINTAINER Christian Proinger <cproinger@mailinator.com>

#from https://github.com/deadtrickster/prometheus_rabbitmq_exporter #/usr/lib/rabbitmq/lib/rabbitmq_server-3.6.6/plugins
COPY prometheus_rabbitmq_exporter-v3.6.5.6.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.6/plugins/prometheus_rabbitmq_exporter-v3.6.5.6.ez
COPY prometheus_process_collector-1.0.0.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.6/plugins/prometheus_process_collector-1.0.0.ez
COPY prometheus-3.0.1.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.6/plugins/prometheus-3.0.1.ez

RUN rabbitmq-plugins enable --offline prometheus
RUN rabbitmq-plugins enable --offline prometheus_process_collector
RUN rabbitmq-plugins enable --offline prometheus_rabbitmq_exporter
