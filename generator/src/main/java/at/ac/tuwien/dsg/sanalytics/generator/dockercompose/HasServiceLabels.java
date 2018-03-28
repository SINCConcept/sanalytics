package at.ac.tuwien.dsg.sanalytics.generator.dockercompose;

import java.util.Map;
import java.util.Optional;

public interface HasServiceLabels {
	/**
	 * <ul>
	 * <li>at.ac.tuwien.dsg.sanalytics.slicemon.active=true/false ... to
	 * enable/disable the prom-scrape-config</li>
	 * <li>at.ac.tuwien.dsg.sanalytics.metricspath=/metrics</li>
	 * <li>at.ac.tuwien.dsg.sanalytics.metricsport=default</li>
	 * <li>at.ac.tuwien.dsg.sanalytics.vm=none|yes ... because memory
	 * monitoring is different for vm-apps as for native apps.</li>
	 * </ul>
	 */
	Map<String, String> getLabels();

	default String getLabelValue(String label, String defaultValue) {
		final String key = label;
		String defaultActive = defaultValue;
		String active = Optional.ofNullable(getLabels()).map(m -> m.getOrDefault(key, defaultActive))
				.orElse(defaultActive);
		return active;
	}

	default String getMetricsPath() {
		return getLabelValue("at.ac.tuwien.dsg.sanalytics.metricspath", null);
	}

	default String getMetricsPort() {
		return getLabelValue("at.ac.tuwien.dsg.sanalytics.metricsport", null);
	}

	default boolean isSlicemonActive() {
		String active = getLabelValue("at.ac.tuwien.dsg.sanalytics.active", "true");
		return "true".equalsIgnoreCase(active);
	}
}
