package at.ac.tuwien.dsg.sanalytics.cli.dockercompose;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(value = "environment")
@JsonInclude(Include.NON_NULL)
public class Service implements HasServiceLabels {

	private String image;
	private Deploy deploy;
	private List<String> ports;
	private List<String> networks;
	private List<String> dependsOn;
	
	private Map<String, String> labels;
}
