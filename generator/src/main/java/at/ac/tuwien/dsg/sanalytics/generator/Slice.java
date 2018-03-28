package at.ac.tuwien.dsg.sanalytics.generator;

import java.util.HashSet;
import java.util.Set;

public class Slice {
	private String name;
	
	private Set<Subslice> subslices = new HashSet<>();

	public Slice(String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}