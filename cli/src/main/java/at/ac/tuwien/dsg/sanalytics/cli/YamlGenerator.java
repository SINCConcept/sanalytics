package at.ac.tuwien.dsg.sanalytics.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import at.ac.tuwien.dsg.sanalytics.cli.PrometheusConfigFactory.PlatformScrapeConfig;
import at.ac.tuwien.dsg.sanalytics.cli.dockercompose.DockerComposeConfig;
import at.ac.tuwien.dsg.sanalytics.cli.promconfig.PrometheusConfig;

public class YamlGenerator {

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {

		YAMLFactory yamlFactory = new YAMLFactory();
		ObjectMapper om = new ObjectMapper(yamlFactory).setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		File src = new File("src/test/resources/docker-compose-example.yml");
		// yamlFactory.createJsonParser(src).nextToken()
		DockerComposeConfig dockerCompose = om.readValue(src, DockerComposeConfig.class);

		// om.writeValue(System.out, dockerCompose);
		PrometheusConfig c = PrometheusConfigFactory.withPlatformScrapeConfig(PlatformScrapeConfig.FEDERATE)
				.createFrom(dockerCompose.getSanalyticsExtension(), dockerCompose);
		
		File resultDir = new File("target/output/");
		resultDir.mkdirs();
		
		File outFile = resultDir.toPath().resolve(toNoExtensionFilename(src) + ".prometheus-config.yml").toFile();
		om.writeValue(outFile, c);
		MermaidJSConfiggFactory.createFrom(dockerCompose);

		enhanceWithSAnalyticsLabels(src, resultDir);

	}

	private static void enhanceWithSAnalyticsLabels(File src, File resultDir)
			throws FileNotFoundException, IOException {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		Map<String, Object> map = (Map<String, Object>) yaml.load(new FileInputStream(src));
		Map<String, Object> sAnalyticsMap = (Map<String, Object>) map.get("x-sanalytics");
		sAnalyticsMap.put("test", "value");
		yaml.dump(map, new FileWriter(
				resultDir.toPath().resolve(toNoExtensionFilename(src) + ".sanalytics-enhanced.yml").toFile()));
	}

	private static String toNoExtensionFilename(File src) {
		return src.getName().substring(0, src.getName().lastIndexOf('.'));
	}
}
