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
	
	@ServiceActivator(inputChannel = "outboundChannel")
	public void saveEvent(GenericMessage<?> message) {
		LOG.info("message: " + message);
		if(message.getPayload() instanceof RandomCount) {
			repo.save((RandomCount) message.getPayload());
			eventsSent.inc();
		} else {
			LOG.error("cannot save message payload of type " + message.getPayload().getClass());
		}
	}
}
