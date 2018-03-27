package at.ac.tuwien.dsg.sanalytics.cli.promconfig;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class DnsSdConfig {
	
	public enum DnsQueryType {
		SRV, A, AAA;
	}

	private DnsQueryType type = DnsQueryType.A;
	private Integer port;
	private String refreshIntervall;
	private List<String> names;
	
	
}
