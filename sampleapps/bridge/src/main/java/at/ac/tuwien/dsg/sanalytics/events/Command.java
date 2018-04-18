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

}
