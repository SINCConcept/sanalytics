docker service create --network=platform_overlay -p 9999:9090 --name haproxy-slice2 -e SANALYTICS_SLICE=slice2 -e SANALYTICS_SUBSLICE=cloud --mount type=bind,source=/mydocker/haproxy,target=/usr/local/etc/haproxy haproxy:1.8.5-alpine