package at.ac.tuwien.dsg.sanalytics.cli;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import at.ac.tuwien.dsg.sanalytics.cli.PrometheusConfigFactory.PlatformScrapeConfig;
import at.ac.tuwien.dsg.sanalytics.cli.dockercompose.DockerComposeConfig;
import at.ac.tuwien.dsg.sanalytics.cli.promconfig.PrometheusConfig;

public class SliceAnalyticsConfigurationGenerator {

	private static class Generator {
		private Yaml yaml;
		private ObjectMapper mapper;
		// TODO
		private String sliceId = "slice1";

		private Generator() {
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(FlowStyle.BLOCK);
			yaml = new Yaml(options);

			mapper = new ObjectMapper(new YAMLFactory()).setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		}

		public void generate() throws IOException {

			Map<String, Object> conf = (Map<String, Object>) yaml
					.load(new FileReader(new File("src/test/resources/my-orchestration-format.yml")));

			File outdir = new File("target/output/slice");
			outdir.mkdirs();

			Map<String, Object> subslices = (Map<String, Object>) conf.get("subslices");
			Map<String, DockerComposeConfig> dockerComposeConfigs = new LinkedHashMap<>();

			for (Entry<String, Object> entry : subslices.entrySet()) {
				Map<String, Object> sourceSubSlice = (Map<String, Object>) entry.getValue();
				// copy so the version is nicely on top of the resulting file.
				Map<String, Object> subslice = new LinkedHashMap<>();
				subslice.put("version", "3.4");
				subslice.putAll(sourceSubSlice);

				// write prometheus config
				DockerComposeConfig dcc = mapper.convertValue(subslice, DockerComposeConfig.class);
				SubsliceId subsliceId = new SimpleSubliceId(sliceId, entry.getKey());
				PrometheusConfig promConfig = PrometheusConfigFactory
						.withPlatformScrapeConfig(PlatformScrapeConfig.FEDERATE).createFrom(subsliceId, dcc);

				// we use a copy so we can modify it later on.
				dockerComposeConfigs.put(entry.getKey(), mapper.convertValue(subslice, DockerComposeConfig.class));

				File pcOutFile = outdir.toPath().resolve(entry.getKey() + ".prometheus.yml").toFile();
				try (Writer writer = new FileWriter(pcOutFile)) {
					mapper.writeValue(writer, promConfig);
				}

				// enhance docker compose-file with labels
				{
					Map<String, Object> services = (Map<String, Object>) subslice.get("services");
					for (Entry<String, Object> serviceEntry : services.entrySet()) {
						Map<String, Object> service = (Map<String, Object>) serviceEntry.getValue();
						Map<String, Object> labels = (Map<String, Object>) service.get("labels");
						if (labels == null) {
							labels = new LinkedHashMap<>();
							service.put("labels", labels);
						}
						labels.put("at.ac.tuwien.dsg.sanalytics.slice", subsliceId.getSlicename());
						labels.put("at.ac.tuwien.dsg.sanalytics.subslice", subsliceId.getSubslice());
					}
				}
				// add prometheus
				{
					Map<String, Object> services = (Map<String, Object>) subslice.get("services");
					Map<String, Object> prometheusService = createPrometheusService();
					services.put("prometheus", prometheusService);
				}

				// dump the docker file
				File dcOutFile = outdir.toPath().resolve(entry.getKey() + ".docker-compose.yml").toFile();
				try (Writer writer = new FileWriter(dcOutFile)) {
					yaml.dump(subslice, writer);
				}

			}
			File mermaidOutFile = outdir.toPath().resolve(sliceId + ".mermaid.mmdc").toFile();
			String mermaidGraphDSLString = MermaidJSConfiggFactory.createFrom(dockerComposeConfigs);
			try (Writer writer = new FileWriter(mermaidOutFile)) {
				writer.write(mermaidGraphDSLString);
				writer.flush();
			}

		}

		private Map<String, Object> createPrometheusService() {
			Map<String, Object> prometheusService = new LinkedHashMap<>();
			prometheusService.put("image", "prom/prometheus:v1.5.3");
			/*
				deploy: 
				      resources: 
				        limits: 
				          memory: 180M
				          cpus: "1.0"
			 */
			Map<String, Object> deploy = new LinkedHashMap<>();
			prometheusService.put("deploy", deploy);
			
			Map<String, Object> resources = new LinkedHashMap<>();
			deploy.put("resources", resources);
			
			Map<String, Object> limits = new LinkedHashMap<>();
			limits.put("memory", "180M");
			limits.put("cpus", "1.0");
			resources.put("limits", limits);
			
			// expose 9090 on random port. 
			prometheusService.put("ports", Arrays.asList("9090"));
			
			List<String> volumes = new ArrayList<>();
			// rules and config
			volumes.add("/mastergit/sanalytics/sampleapps/prom/:/etc/prometheus/");
			// data-directory
//			volumes.add("/c/Users/cproinger/Documents/Docker/prom/cloud:/prometheus");
			prometheusService.put("volumes", volumes);

			List<String> commands = new ArrayList<>();
			commands.add("-config.file=/etc/prometheus/prometheus-cloud.yml");
			commands.add("-storage.local.path=/prometheus");
			commands.add("-web.console.libraries=/etc/prometheus/console_libraries");
			commands.add("-web.console.templates=/etc/prometheus/consoles");
			commands.add("-storage.remote.influxdb-url=http://influx:8086");
			commands.add("-storage.remote.influxdb.database=mytestdb");
			commands.add("-storage.remote.influxdb.retention-policy=autogen");
			commands.add("-storage.remote.influxdb.username=username");
			prometheusService.put("command", commands);
			
			List<String> envVars = new ArrayList<>();
			envVars.add("INFLUXDB_PW=password");
			
			return prometheusService;
		}
	}

	public static void main(String[] args) throws IOException {
		new Generator().generate();
	}
}
