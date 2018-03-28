package at.ac.tuwien.dsg.sanalytics.generator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import at.ac.tuwien.dsg.sanalytics.generator.dockercompose.DockerComposeConfig;
import at.ac.tuwien.dsg.sanalytics.generator.dockercompose.HasServiceLabels;
import at.ac.tuwien.dsg.sanalytics.generator.dockercompose.Service;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.DnsSdConfig;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.GlobalConfig;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.PrometheusConfig;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.ScrapeConfig;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.StaticConfig;
import at.ac.tuwien.dsg.sanalytics.generator.promconfig.DnsSdConfig.DnsQueryType;

public class PrometheusConfigFactory {

	public enum PlatformScrapeConfig {
		/**
		 * No HA-Proxy in between that handles the filtering
		 */
		FEDERATE,
		/**
		 * use simple metrics endpoint if filtering HA-Proxy is in place. 
		 */
		METRICS;
	}

	private PlatformScrapeConfig platformScrapeConfig;
	private String promPlatformTarget = "prom_platform:9090";

	public PrometheusConfigFactory(PlatformScrapeConfig platformScrapeConfig) {
		this.platformScrapeConfig = platformScrapeConfig;
	}

	public static PrometheusConfigFactory withPlatformScrapeConfig(PlatformScrapeConfig platformScrapeConfig) {
		return new PrometheusConfigFactory(platformScrapeConfig);
	}

	public PrometheusConfig createFrom(SubsliceId subsliceId, DockerComposeConfig dockerCompose) {
		PrometheusConfig c = new PrometheusConfig();

		// String slicename = "slicename";
		// String subsliceName = "cloud";
		String slicename = subsliceId.getSlicename();
		String subsliceName = subsliceId.getSubslice();
		c.setGlobal(createPrometheusGlobalConig(slicename, subsliceName));

		ScrapeConfig scrapeConf = createPromPlatformScrapeConfig(slicename, subsliceName);
		c.getScrapeConfigs().add(scrapeConf);

		for (Entry<String, Service> entry : dockerCompose.getServices().entrySet()) {
			if (entry.getValue().getDeploy().isSlicemonActive()) {
				c.getScrapeConfigs().add(createScrapeConfigFromService(entry.getKey(), entry.getValue()));
			}
		}
		return c;
	}

	private GlobalConfig createPrometheusGlobalConig(String slicename, String subsliceName) {
		GlobalConfig gc = new GlobalConfig();

		Map<String, String> externalLabels = new HashMap<>();
		externalLabels.put("slice", slicename);
		externalLabels.put("subslice", subsliceName);
		gc.setExternalLabels(externalLabels);
		return gc;
	}

	private ScrapeConfig createScrapeConfigFromService(String serviceName, Service service) {
		HasServiceLabels hasServiceLabels = service.getDeploy();

		ScrapeConfig sc = new ScrapeConfig();
		sc.setJobName(serviceName);
		sc.setHonorLabels(false);
		sc.setMetricsPath(hasServiceLabels.getMetricsPath());

		DnsSdConfig dnsSdConfig = new DnsSdConfig();
		parseToInt(hasServiceLabels.getMetricsPort()).ifPresent(i -> dnsSdConfig.setPort(i));
		dnsSdConfig.setType(DnsQueryType.A);
		dnsSdConfig.setNames(Arrays.asList("tasks." + serviceName));

		sc.setDnsSdConfigs(Arrays.asList(dnsSdConfig));
		return sc;
	}

	private Optional<Integer> parseToInt(String sInt) {
		return sInt == null ? Optional.empty() : Optional.of(Integer.valueOf(sInt));
	}

	private ScrapeConfig createPromPlatformScrapeConfig(String slicename, String subsliceName) {
		ScrapeConfig scrapeConf = new ScrapeConfig();
		scrapeConf.setJobName(subsliceName + "Platform");
		scrapeConf.setHonorLabels(true);

		if (platformScrapeConfig == PlatformScrapeConfig.FEDERATE) {
			scrapeConf.setMetricsPath("/federate");
			scrapeConf.setParams(createMapFrom("match[]", "{cl_aatd_sanalytics_slice=\"" + slicename + "\"}",
					"{cl_aatd_sanalytics_subslice=\"" + subsliceName + "\"}"));
		} else {
			scrapeConf.setMetricsPath("/metrics");
		}

		StaticConfig staticConf = new StaticConfig();
		staticConf.setTargets(Arrays.asList(promPlatformTarget));
		scrapeConf.setStaticConfigs(Arrays.asList(staticConf));
		return scrapeConf;
	}

	private Map<String, List<String>> createMapFrom(String name, String... value) {
		HashMap<String, List<String>> m = new HashMap<>();
		m.put(name, Arrays.asList(value));
		return m;
	}
}
