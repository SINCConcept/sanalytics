package at.ac.tuwien.dsg.sanalytics.generator.dockercompose;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class Resources {
	
	private Limits limits;
	
	private Limits reservations;
}
