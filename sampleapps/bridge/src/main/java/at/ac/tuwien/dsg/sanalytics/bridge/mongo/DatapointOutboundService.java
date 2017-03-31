package at.ac.tuwien.dsg.sanalytics.bridge.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.sanalytics.events.DatapointCEPResult;

@Profile({"outbound-mongo-datapoint"})
@Component
public class DatapointOutboundService extends AbstractOutboundMongoService {
	
	private final static Logger LOG = LoggerFactory.getLogger(DatapointOutboundService.class);
	
	@Autowired
	private DatapointCEPResultRepository repo;
	
	@ServiceActivator(inputChannel = "outboundChannel")
	public void saveEvent(GenericMessage<?> message) {
		LOG.info("message: " + message);
		eventSaveLatency.time(() -> {
			if(message.getPayload() instanceof DatapointCEPResult) {
				repo.save((DatapointCEPResult) message.getPayload());
				eventsSent.inc();
			} else {
				LOG.error("cannot save message payload of type " + message.getPayload().getClass());
			}
		});
	}
}
