package at.ac.tuwien.dsg.sanalytics.events;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class DatapointCEPResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	private Map<String, Object> data;

	private Date time;
	
	public DatapointCEPResult() {
		//empty
	}

	public DatapointCEPResult(Map<String, Object> data) {
		this.data = data;
		this.time = new Date();
	}
	
	public Map<String, Object> getData() {
		return data;
	}
	
	public Date getTime() {
		return time;
	}
}
