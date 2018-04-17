package at.ac.tuwien.dsg.sanalytics.generator.promconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class RemoteReadWrite {

	private String url;
	private String remoteTimeout = "30s";
	private BasicAuth basicAuth;
	private String database;
	private String retentionPolicy;
	
}
