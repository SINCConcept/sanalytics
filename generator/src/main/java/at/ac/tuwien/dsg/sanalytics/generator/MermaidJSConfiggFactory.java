package at.ac.tuwien.dsg.sanalytics.generator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import at.ac.tuwien.dsg.sanalytics.generator.dockercompose.DockerComposeConfig;
import at.ac.tuwien.dsg.sanalytics.generator.dockercompose.Service;

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
				sb.append("  ").append(metricName(dcConfigEntry.getKey(), serviceEntry))
						.append(alias(dcConfigEntry.getKey(), serviceEntry)).append(System.lineSeparator());
				List<String> dependsOn = serviceEntry.getValue().getDependsOn();
				if (dependsOn != null) {
					// the subgraph of the dependency should always be already
					// defined so that service is in the correct subgraph
					// currently that works if one defines cloud first, then
					// nfv, then iot.
					dependsOn.stream().map(dep -> lookup(dep, dockerComposeConfigs))
							.map(dep -> metricName(dcConfigEntry.getKey(), serviceEntry) + " --- " + dep)
							.forEach(s -> sb.append("  ").append(s).append(System.lineSeparator()));
				}
			}

			sb.append("end").append(System.lineSeparator());
		}

		return sb.toString();
	}

	private static String metricName(String subslice, Entry<String, Service> serviceEntry) {
		return "job." + subslice + "." + serviceEntry.getKey();
	}

	private static String alias(String subslice, Entry<String, Service> serviceEntry) {
		return "[" + serviceEntry.getKey() + "]";
	}

	private static String lookup(String dep, Map<String, DockerComposeConfig> dockerComposeConfigs) {
		return dockerComposeConfigs.entrySet().stream()
			.flatMap(e -> e.getValue().getServices().entrySet().stream().map(se -> Pair.of(e.getKey(), se)))
			.filter(p -> p.getValue().getKey().equals(dep))
			.map(p -> metricName(p.getKey(), p.getValue()))
			.findFirst().get();
	}

}
