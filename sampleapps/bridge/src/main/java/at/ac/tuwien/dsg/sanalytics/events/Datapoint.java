package at.ac.tuwien.dsg.sanalytics.events;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtils;

public class Datapoint implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String station;
	private String datapoint;
	private double value;

	public Datapoint() {
		//empty
	}
	
	public Datapoint(String station, String datapoint, double value) {
		this.station = station;
		this.datapoint = datapoint;
		this.value = value;
	}

	public Datapoint(HashMap<String, Object> m) {
		this.station = (String) m.get("station");
		this.datapoint = (String) m.get("datapoint");
		this.value = ((Double) m.get("value")).doubleValue();
	}

	public String getStation() {
		return station;
	}

	public String getDatapoint() {
		return datapoint;
	}

	public double getValue() {
		return value;
	}

	public static Datapoint from(String topic, Double v) {
		String[] topicParts = topic.split("/");
		Datapoint d = new Datapoint(topicParts[1], topicParts[3], v);
		return d;
	}
	
	public static Datapoint from(String payload) {
		String[] parts = payload.split(";");
		if(parts.length != 3) {
			throw new IllegalArgumentException("cannot parse payload: " + payload);
		}
		Datapoint d = new Datapoint(parts[0], parts[1], Double.valueOf(parts[2]));
		return d;
	}

	@Override
	public String toString() {
		return "Datapoint [station=" + station + ", datapoint=" + datapoint + ", value=" + value + "]";
	}

	
	
}
