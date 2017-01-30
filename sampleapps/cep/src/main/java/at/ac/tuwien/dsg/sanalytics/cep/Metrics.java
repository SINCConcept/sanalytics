package at.ac.tuwien.dsg.sanalytics.cep;

import io.prometheus.client.Counter;

public class Metrics {

	final static Counter EVENTS_SAVED = Counter.build()
			.name("events_saved_total")
			.help("Total number of events saved to backend store")
			.register();
	
	static final Counter MESSAGES = Counter.build()
			.name("messages_total")
			.help("Total number of messages received via mqtt")
			.register();

}
