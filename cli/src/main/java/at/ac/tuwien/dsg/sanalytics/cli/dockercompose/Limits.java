package at.ac.tuwien.dsg.sanalytics.cli.dockercompose;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class Limits {
	/**
	 * we could derive alarm thresholds from this maybe. 
	 * 
	 * values like '50M'. 
	 */
	private String memory;
	/**
	 * '0.50' means 50% of available processing time. 
	 */
	private String cpus;
}
