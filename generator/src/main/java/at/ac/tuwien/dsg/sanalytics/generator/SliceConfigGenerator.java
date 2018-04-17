package at.ac.tuwien.dsg.sanalytics.generator;

import java.io.IOException;
import java.io.Reader;
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

import at.ac.tuwien.dsg.sanalytics.generator.PrometheusConfigFactory.PlatformScrapeConfig;
import at.ac.tuwien.dsg.sanalytics.generator.dockercompose.DockerComposeConfig;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.BasicAuth;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.PrometheusConfig;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.RemoteReadWrite;
import at.ac.tuwien.dsg.sanalytics.generator.sliceconfig.GlobalMonitoringConfiguration;

public class SliceConfigGenerator {
	private static final String DOCKER_COMPOSE_VERSION = "3.4";

	private final static java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(SliceConfigGenerator.class.getName());

	private final Yaml yaml;
	private final ObjectMapper mapper;
	private final String sliceId;
	private final Reader orchestrationFormatReader;
	private final WriterProvider writerProvider;

	public SliceConfigGenerator(String sliceId, Reader orchestrationFormatReader,
			WriterProvider writerProvider) {
		this.sliceId = sliceId;
		this.orchestrationFormatReader = orchestrationFormatReader;
		this.writerProvider = writerProvider;

		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		yaml = new Yaml(options);

		mapper = new ObjectMapper(new YAMLFactory())
				.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
	}

	public void generate() throws IOException {

		Map<String, Object> conf = (Map<String, Object>) yaml.load(orchestrationFormatReader);

		GlobalMonitoringConfiguration globalMonConf = extractGlobalMonitoringConfiguration(conf);

		Map<String, Object> subslices = (Map<String, Object>) conf.get("subslices");
		LOG.info("identified sub-slices: " + subslices.keySet());

		Map<String, DockerComposeConfig> dockerComposeConfigs = new LinkedHashMap<>();

		for (Entry<String, Object> entry : subslices.entrySet()) {
			Map<String, Object> subslice = copyWithDockerComposeVersion(
					(Map<String, Object>) entry.getValue());
			String subsliceName = entry.getKey();

			// write prometheus config
			DockerComposeConfig dcc = mapper.convertValue(subslice, DockerComposeConfig.class);
			SubsliceId subsliceId = new SimpleSubliceId(sliceId, entry.getKey());
			PrometheusConfig promConfig = PrometheusConfigFactory
					.withPlatformScrapeConfig(PlatformScrapeConfig.FEDERATE)
					.createFrom(subsliceId, dcc);

			// we use a copy so we can modify it later on.
			dockerComposeConfigs.put(entry.getKey(),
					mapper.convertValue(subslice, DockerComposeConfig.class));

			try (Writer writer = writerProvider.getWriter(subsliceName, "prometheus.yml")) {
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
				Map<String, Object> prometheusService = createPrometheusService(subsliceName,
						globalMonConf);
				services.put("prometheus", prometheusService);
			}

			// dump the docker file
			try (Writer writer = writerProvider.getWriter(subsliceName, "docker-compose.yml")) {
				yaml.dump(subslice, writer);
			}

		}
		String mermaidGraphDSLString = MermaidJSConfiggFactory.createFrom(dockerComposeConfigs);
		try (Writer writer = writerProvider.getWriter(null, "mermaid.mmdc")) {
			writer.write(mermaidGraphDSLString);
			writer.flush();
		}

	}

	private GlobalMonitoringConfiguration extractGlobalMonitoringConfiguration(
			Map<String, Object> conf) {
		Map<String, Object> globalMon = (Map<String, Object>) conf.get("globalMonitoring");
		if (globalMon == null) {
			GlobalMonitoringConfiguration c = new GlobalMonitoringConfiguration();
			RemoteReadWrite remoteWrite = new RemoteReadWrite();
			remoteWrite.setUrl("http://influx:8086");
			remoteWrite.setBasicAuth(new BasicAuth("username", "password"));
			remoteWrite.setDatabase("mytestdb");
			c.setRemoteWrite(remoteWrite);
			return c;
		}

		return mapper.convertValue(conf, GlobalMonitoringConfiguration.class);

	}

	/**
	 * copy so the version is nicely on top of the resulting file.
	 */
	private Map<String, Object> copyWithDockerComposeVersion(Map<String, Object> sourceSubSlice) {
		Map<String, Object> subslice = new LinkedHashMap<>();
		subslice.put("version", DOCKER_COMPOSE_VERSION);
		subslice.putAll(sourceSubSlice);
		return subslice;
	}

	/**
	 * @return docker-compose configuration for a sub-slice prometheus instance. 
	 */
	private Map<String, Object> createPrometheusService(String subsliceName,
			GlobalMonitoringConfiguration globalMonConf) {
		Map<String, Object> prometheusService = new LinkedHashMap<>();
		prometheusService.put("image", "prom/prometheus:v1.5.3");
		/*
		 * deploy: resources: limits: memory: 180M cpus: "1.0"
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
		// rules and config (hm. relative mount? or create config?)
		// or use volume?
		volumes.add("./prom/conf/" + ":/etc/prometheus/");
		// data-directory
		// prometheus doesn't like it if this path is a folder on a windows machine
		// shared with the VM docker is running on. 
		// volumes.add("/c/Users/cproinger/Documents/Docker/prom/cloud:/prometheus");
		volumes.add("./prom/data/" + subsliceName + ":/prometheus");
		prometheusService.put("volumes", volumes);

		List<String> commands = new ArrayList<>();
		// hm. use only prometheus.yml instead of prometheus-<subsliceName>.yml
//		commands.add("-config.file=/etc/prometheus/prometheus-" + subsliceName + ".yml");
		commands.add("-config.file=/etc/prometheus/prometheus.yml");
		commands.add("-storage.local.path=/prometheus");
		commands.add("-web.console.libraries=/etc/prometheus/console_libraries");
		commands.add("-web.console.templates=/etc/prometheus/consoles");
		
		// from https://github.com/ContainerSolutions/prometheus-swarm-discovery/blob/master/docker-compose.yaml
		commands.add("-storage.local.retention=2h");
		commands.add("-storage.local.memory-chunks=1048576");
		
		commands.add("-storage.remote.influxdb-url=" + globalMonConf.getRemoteWrite().getUrl());
		// TODO extract as parameters
		commands.add("-storage.remote.influxdb.database=" + "mytestdb");
		commands.add("-storage.remote.influxdb.retention-policy=" + "autogen");
		
		// auth
		commands.add("-storage.remote.influxdb.username="
				+ globalMonConf.getRemoteWrite().getBasicAuth().getUsername());
		prometheusService.put("command", commands);
		List<String> envVars = new ArrayList<>();
		envVars.add("INFLUXDB_PW=" + globalMonConf.getRemoteWrite().getBasicAuth().getPassword());
		prometheusService.put("environment", envVars);
		
		return prometheusService;
	}
}