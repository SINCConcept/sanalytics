docker service create --network custom_overlay --name cep --container-label sanalytics.slice="slice0" --label sanalytics.service.slice="slice0" cproinger/sample-cep