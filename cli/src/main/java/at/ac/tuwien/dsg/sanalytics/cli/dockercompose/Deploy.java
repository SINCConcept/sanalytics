package at.ac.tuwien.dsg.sanalytics.cli.dockercompose;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Deploy implements HasServiceLabels {

	/**
	 * we can derive the target service metric from this.
	 */
	@JsonInclude(Include.NON_NULL)
	private Integer replicas;

	// endpoint_mode: vip or dnsrr, not that important for us. besides v 3.3.
	// only
	// mode: global or replicated, hm. we don't need that.
	// placement: not important for us.
	@JsonInclude(Include.NON_NULL)
	private Resources resources;

	@JsonInclude(Include.NON_NULL)
	private RestartPolicy restartPolicy;

	/**
	 * we could derive something from special labels. (These labels are only set
	 * on the service, and not on any containers for the service. To set labels
	 * on containers instead, use the labels key outside of deploy.)
	 */
	@JsonInclude(Include.NON_NULL)
	private Map<String, String> labels;
}
