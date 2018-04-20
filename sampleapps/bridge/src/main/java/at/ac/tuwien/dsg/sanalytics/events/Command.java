package at.ac.tuwien.dsg.sanalytics.events;

import java.io.Serializable;

public class Command implements Serializable {

	private static final long serialVersionUID = 1L;

	private String actuatorId;
	private String datapointId;
	private String command;

	public String getActuatorId() {
		return actuatorId;
	}

	public void setActuatorId(String actuatorId) {
		this.actuatorId = actuatorId;
	}

	public String getDatapointId() {
		return datapointId;
	}

	public void setDatapointId(String datapointId) {
		this.datapointId = datapointId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "Command [actuatorId=" + actuatorId + ", datapointId=" + datapointId + ", command="
				+ command + "]";
	}

	public static Command from(String payload) {
		String[] parts = payload.split(";");
		if (parts.length != 3) {
			throw new IllegalArgumentException("cannot parse payload: " + payload);
		}
		Command d = new Command();
		d.actuatorId = parts[0]; 
		d.datapointId = parts[1];
		d.command = parts[2];
		return d;
	}

}
