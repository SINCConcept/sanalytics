package at.ac.tuwien.dsg.sanalytics.meta;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

@Embeddable
public class SubSliceConfigurationFile {

	private String type;
	
	@Lob
	private String content;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
}
