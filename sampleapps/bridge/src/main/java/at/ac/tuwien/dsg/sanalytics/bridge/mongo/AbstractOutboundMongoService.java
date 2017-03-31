package at.ac.tuwien.dsg.sanalytics.bridge.mongo;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

public abstract class AbstractOutboundMongoService {

	Counter eventsSent = Counter.build()
			.name("mongo_events_saved_total")
			.help("Total number of events saved to backend store")
			.register();
	
	
	Summary eventSaveLatency = Summary.build()
			.quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
            .quantile(0.9, 0.01)   // Add 90th percentile with 1% tolerated error
            .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
			.name("mongo_events_saved_latency")
			.help("Total number of events saved to backend store")
			.register();

}
