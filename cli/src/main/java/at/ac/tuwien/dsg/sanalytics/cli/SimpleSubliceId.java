package at.ac.tuwien.dsg.sanalytics.cli;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimpleSubliceId implements SubsliceId{

	private String slicename;
	private String subslice;
}
