package at.ac.tuwien.dsg.sanalytics.cli;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
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

				// dump the docker file
				File dcOutFile = outdir.toPath().resolve(entry.getKey() + ".docker-compose.yml").toFile();
				try (Writer writer = new FileWriter(dcOutFile)) {
					yaml.dump(subslice, writer);
				}

				// write prometheus config
				DockerComposeConfig dcc = mapper.convertValue(subslice, DockerComposeConfig.class);
				SubsliceId subsliceId = new SimpleSubliceId(sliceId, entry.getKey());
				PrometheusConfig promConfig = PrometheusConfigFactory
						.withPlatformScrapeConfig(PlatformScrapeConfig.FEDERATE)
						.createFrom(subsliceId, dcc);
				
				// we use a copy so we can modify it later on. 
				dockerComposeConfigs.put(entry.getKey(), mapper.convertValue(subslice, DockerComposeConfig.class));
				
				File pcOutFile = outdir.toPath().resolve(entry.getKey() + ".prometheus.yml").toFile();
				try (Writer writer = new FileWriter(pcOutFile)) {
					mapper.writeValue(writer, promConfig);
				}
				
			}
			File mermaidOutFile = outdir.toPath().resolve(sliceId + ".mermaid.mmdc").toFile();
			String mermaidGraphDSLString = MermaidJSConfiggFactory.createFrom(dockerComposeConfigs);
			try(Writer writer = new FileWriter(mermaidOutFile)) {
				writer.write(mermaidGraphDSLString);
				writer.flush();
			}
			
		}
	}

	public static void main(String[] args) throws IOException {
		new Generator().generate();
	}
}
