package at.ac.tuwien.dsg.sanalytics.meta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;

@Entity
public class SubsliceMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
	private Slice slice;

	private String name;

	@ElementCollection
	private Set<SubSliceConfigurationFile> configurationFiles = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Slice getSlice() {
		return slice;
	}

	public void setSlice(Slice slice) {
		this.slice = slice;
	}

	public Set<SubSliceConfigurationFile> getConfigurationFiles() {
		return configurationFiles;
	}

	public void addConfigurationFile(SubSliceConfigurationFile f) {
		Optional<SubSliceConfigurationFile> existing = configurationFiles.stream()
				.filter(cf -> Objects.equals(cf.getType(), f.getType())).findFirst();
		existing.ifPresent(cf -> configurationFiles.remove(cf));
		configurationFiles.add(f);
	}
}
