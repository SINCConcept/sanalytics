package at.ac.tuwien.dsg.sanalytics.bridge.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.GenericMessage;

import at.ac.tuwien.dsg.sanalytics.events.RandomCount;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

@Configuration
@Profile("outbound-mongo")
@ImportAutoConfiguration({MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableMongoRepositories(basePackageClasses = OutboundMongoConfig.class)
public class OutboundMongoConfig {

	private final static Logger LOG = LoggerFactory.getLogger(OutboundMongoConfig.class);
	
	@Autowired
	private RandomCountRepository repo;
	
	private Counter eventsSent = Counter.build()
			.name("mongo_events_saved_total")
			.help("Total number of events saved to backend store")
			.register();
	
	
	private Summary eventSaveLatency = Summary.build()
			.quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
            .quantile(0.9, 0.01)   // Add 90th percentile with 1% tolerated error
            .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
			.name("mongo_events_saved_latency")
			.help("Total number of events saved to backend store")
			.register();
	
	
	@ServiceActivator(inputChannel = "outboundChannel")
	public void saveEvent(GenericMessage<?> message) {
		LOG.info("message: " + message);
		eventSaveLatency.time(() -> {
			if(message.getPayload() instanceof RandomCount) {
				repo.save((RandomCount) message.getPayload());
				eventsSent.inc();
			} else {
				LOG.error("cannot save message payload of type " + message.getPayload().getClass());
			}
		});
	}
}
