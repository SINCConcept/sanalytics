package at.ac.tuwien.dsg.sanalytics.bridge.esper;

import io.prometheus.client.Counter;

public abstract class AbstractAnalyzer {

	Counter eventsSent = Counter.build()
			.name("cep_events_sent_total")
			.help("Total number of events sent to backend store")
			.register();
	
	Counter messages = Counter.build()
			.name("cep_messages_processed_total")
			.help("Total number of messages received via mqtt")
			.register();
}
