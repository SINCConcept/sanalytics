package at.ac.tuwien.dsg.sanalytics.meta;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.CascadableDescriptor;

import org.springframework.data.rest.core.annotation.RestResource;

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
	
	@OneToMany(mappedBy = "slice", cascade = CascadeType.ALL,orphanRemoval = true)
	@RestResource(exported = true)
	private Set<SubsliceMetadata> subslices = new HashSet<>();
	
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
	
	public Set<SubsliceMetadata> getSubslices() {
		return subslices;
	}
	
	public void addSubslice(SubsliceMetadata subslice) {
		this.subslices.add(subslice);
	}

	public Slice clear() {
		this.subslices.clear();
		return this;
    }

	public SubsliceMetadata getOrNewSubSlice(String subsliceName) {
		Optional<SubsliceMetadata> oss = subslices.stream().filter(ss -> Objects.equals(ss.getName(), subsliceName))
				.findFirst();
		if (!oss.isPresent()) {
			SubsliceMetadata ssm = new SubsliceMetadata();
			ssm.setName(subsliceName);
			subslices.add(ssm);
			ssm.setSlice(this);
			return ssm;
		}
		
		return oss.get();
	}
}
