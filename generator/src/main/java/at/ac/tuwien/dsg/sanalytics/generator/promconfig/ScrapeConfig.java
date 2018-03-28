package at.ac.tuwien.dsg.sanalytics.generator.promconfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ScrapeConfig {

	private String jobName;
	private boolean honorLabels;
	private String metricsPath;
	@JsonInclude(Include.NON_EMPTY)
	private Map<String, List<String>> params = new HashMap<>();
	
	private List<DnsSdConfig> dnsSdConfigs;
	private List<StaticConfig> staticConfigs;
}
