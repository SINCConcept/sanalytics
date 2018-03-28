package at.ac.tuwien.dsg.sanalytics.generator.dockercompose;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DockerComposeConfig {
	private String version = "3.4";
	
	private Map<String, Service> services = new HashMap<>();
	private Map<String, Network> networks = new HashMap<>();
	
	@JsonProperty("x-sanalytics")
	private SAnalyticsExtension sanalyticsExtension;
}
