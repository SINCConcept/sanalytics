package at.ac.tuwien.dsg.sanalytics.generator.promconfig;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class PrometheusConfig {

	private GlobalConfig global;
	private RemoteReadWrite remoteWrite;
	private RemoteReadWrite remoteRead;
	
	private List<String> ruleFiles = new ArrayList<>();
	
	@JsonProperty(value = "scrape_configs")
	private List<ScrapeConfig> scrapeConfigs = new ArrayList<>();
}
