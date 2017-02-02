package at.ac.tuwien.dsg.sanalytics.events;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;

public class RandomCount implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	private long count;

	private Date time;
	
	public RandomCount(long count, long millis) {
		this.count = count;
		this.time = new Date(millis);
	}
	
	public String getId() {
		return id;
	}
	
	public long getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "RandomCount [id=" + id + ", count=" + count + ", time=" + time + "]";
	}

}
