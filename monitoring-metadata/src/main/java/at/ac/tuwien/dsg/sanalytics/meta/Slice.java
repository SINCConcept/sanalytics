package at.ac.tuwien.dsg.sanalytics.meta;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

@Entity
public class Slice {

	@Id
	private String id;
	
	/**
	 * mermaid-js graph string
	 */
	@NotNull
	@Lob
	private String graphDefinition;
	
	@SuppressWarnings("unused")
	private Slice() {
		//empty: for hibernate
	}
	
	public Slice(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public String getGraphDefinition() {
		return graphDefinition;
	}
	
	public void setGraphDefinition(String graphDefinition) {
		this.graphDefinition = graphDefinition;
	}
}
