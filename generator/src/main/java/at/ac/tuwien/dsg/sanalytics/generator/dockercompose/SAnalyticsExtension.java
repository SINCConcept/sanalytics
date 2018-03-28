package at.ac.tuwien.dsg.sanalytics.generator.dockercompose;

import at.ac.tuwien.dsg.sanalytics.generator.SubsliceId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SAnalyticsExtension implements SubsliceId {
	
	private String slicename;
	private String subslice;
}
