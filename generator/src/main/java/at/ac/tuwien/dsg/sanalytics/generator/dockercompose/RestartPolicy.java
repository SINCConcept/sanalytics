package at.ac.tuwien.dsg.sanalytics.generator.dockercompose;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class RestartPolicy {

	public enum RestartCondition {
		@JsonProperty("none")
		NONE,
		@JsonProperty("on-failure")
		ON_FAILURE,
		@JsonProperty("any")
		ANY;
	}
	
	
	private RestartCondition condition;
	/**
	 * we could derive some time-window-length where alarms are not triggered. 
	 */
	private String delay;
	
	private Integer maxAttempts;
	
	/**
	 * e.g. '120s'
	 */
	private String window;
}
