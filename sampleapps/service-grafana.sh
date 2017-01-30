docker service create \
	-p 3000:3000 \
	--network="custom_monitoring" \
	--name grafana \
	--container-label sanalytics.slice="slice0" \
	grafana/grafana