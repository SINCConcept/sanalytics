package at.ac.tuwien.dsg.sanalytics.generator.promconfig;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrometheusConfig {

	private GlobalConfig global;
	
	private List<String> ruleFiles = new ArrayList<>();
	
	@JsonProperty(value = "scrape_configs")
	private List<ScrapeConfig> scrapeConfigs = new ArrayList<>();
}
