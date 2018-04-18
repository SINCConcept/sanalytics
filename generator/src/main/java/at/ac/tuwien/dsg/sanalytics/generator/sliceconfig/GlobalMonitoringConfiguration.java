package at.ac.tuwien.dsg.sanalytics.generator.sliceconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import at.ac.tuwien.dsg.sanalytics.generator.promconfig.RemoteReadWrite;
import lombok.Getter;
import lombok.Setter;

/**
 * as 'globalMonitoring' at top level in our slice-orchestration format. 
 */
@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class GlobalMonitoringConfiguration {

	private RemoteReadWrite remoteWrite;
	
	/**
	 * additional network to add (used to attach
	 * to the globalmon network if the global-monitoring 
	 * is deployed in the same docker swarm). 
	 */
	private String network;
}
