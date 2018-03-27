can be manually transformed with 

docker run --rm -v /mastergit/sanalytics/monitoring-metadata:/mmdc jnewland/mermaid.cli -i /mmdc/src/test/resources/graphDefinitions/s1.mermaidJS.txt -o /mmdc/target/graph.png -w 1024 -H 400