package at.ac.tuwien.dsg.sanalytics.filterproxy;

import java.util.List;

public interface PrometheusMetricsEndpointLookup {

	public List<String> getAllAddresses(String name);
}
