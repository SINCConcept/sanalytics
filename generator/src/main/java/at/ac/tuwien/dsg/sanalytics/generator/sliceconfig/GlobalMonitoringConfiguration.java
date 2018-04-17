package at.ac.tuwien.dsg.sanalytics.generator.sliceconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import at.ac.tuwien.dsg.sanalytics.generator.promconfig.RemoteReadWrite;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class GlobalMonitoringConfiguration {

	private RemoteReadWrite remoteWrite;
}
