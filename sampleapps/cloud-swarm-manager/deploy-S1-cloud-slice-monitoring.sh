 #sed -e "s/cloudstack/cloudstackS1/" ./configs/prom/prometheus-cloud.yml > ./configs/prom/prometheus-S1-cloud.yml
 docker stack deploy --compose-file dc-S1-cloud-slice-monitoring.yml cloudmonS1 