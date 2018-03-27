package at.ac.tuwien.dsg.sanalytics.cli.promconfig;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class GlobalConfig {

	private String scrapeIntervall;
	private String scrapeTimeout;
	private String evaluationInterval;

	@JsonInclude(value = Include.NON_EMPTY)
	private Map<String, String> externalLabels = new HashMap<>();
}
