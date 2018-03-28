package at.ac.tuwien.dsg.sanalytics.cli;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import at.ac.tuwien.dsg.sanalytics.cli.dockercompose.DockerComposeConfig;
import at.ac.tuwien.dsg.sanalytics.cli.dockercompose.Service;

public class MermaidJSConfiggFactory {

	public static String createFrom(DockerComposeConfig composeConfig) {
		StringBuilder sb = new StringBuilder();

		return sb.toString();
	}

	public static String createFrom(Map<String, DockerComposeConfig> dockerComposeConfigs) {
		StringBuilder sb = new StringBuilder();
		sb.append("graph BT").append(System.lineSeparator());
		for (Entry<String, DockerComposeConfig> dcConfigEntry : dockerComposeConfigs.entrySet()) {
			sb.append("subgraph ").append(dcConfigEntry.getKey()).append(System.lineSeparator());

			for (Entry<String, Service> serviceEntry : dcConfigEntry.getValue().getServices().entrySet()) {
				List<String> dependsOn = serviceEntry.getValue().getDependsOn();
				if (dependsOn != null) {
					dependsOn.stream().map(dep -> lookup(dep, dockerComposeConfigs))
							.map(dep -> serviceEntry.getKey() + " --- " + dep)
							.forEach(s -> sb.append("  ").append(s).append(System.lineSeparator()));
				} else {
					sb.append("  ").append(serviceEntry.getKey()).append(System.lineSeparator());
				}
			}

			sb.append("end").append(System.lineSeparator());
		}

		return sb.toString();
	}

	private static String lookup(String dep, Map<String, DockerComposeConfig> dockerComposeConfigs) {

		return dep;
	}

}
