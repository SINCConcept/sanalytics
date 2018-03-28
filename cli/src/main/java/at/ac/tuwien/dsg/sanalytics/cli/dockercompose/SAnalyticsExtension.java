package at.ac.tuwien.dsg.sanalytics.cli.dockercompose;

import at.ac.tuwien.dsg.sanalytics.cli.SubsliceId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SAnalyticsExtension implements SubsliceId {
	
	private String slicename;
	private String subslice;
}
