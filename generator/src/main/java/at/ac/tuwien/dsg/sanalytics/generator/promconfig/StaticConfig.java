package at.ac.tuwien.dsg.sanalytics.generator.promconfig;

import java.util.ArrayList;
import java.util.List;

public class StaticConfig {

	private List<String> targets = new ArrayList<>();

	public List<String> getTargets() {
		return targets;
	}

	public void setTargets(List<String> targets) {
		this.targets = targets;
	}
}
