mkdir -p /home/docker/myvolumes/prom/platform
docker network create -d overlay custom_monitoring
docker network create -d overlay platform_overlay

docker service create --network=custom_monitoring --network=platform_overlay -p 8888:8080 --mode global --name cadvisor --mount type=bind,source=/,target=/rootfs,readonly=true --mount type=bind,source=/var/run,target=/var/run,readonly=false --mount type=bind,source=/sys,target=/sys,readonly=true --mount type=bind,source=/var/lib/docker/,target=/var/lib/docker,readonly=true google/cadvisor
docker service create -p 9090:9090 --mount type=bind,source=/mastergit/sanalytics/sampleapps/prom/,target=/etc/prometheus/,readonly --mount type=bind,source=/home/docker/myvolumes/prom/platform,target=/prometheus/ --name=prom_platform --network="custom_monitoring" --network="platform_overlay" prom/prometheus:v1.5.3 -config.file=/etc/prometheus/prometheus-platform.yml -storage.local.path=/prometheus -web.console.libraries=/etc/prometheus/console_libraries -web.console.templates=/etc/prometheus/consoles