package at.ac.tuwien.dsg.sanalytics.bridge.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.GenericMessage;

@MessageEndpoint
@Profile("outbound-logger")
public class OutboundLogger {

	private final static Logger LOG = LoggerFactory.getLogger(OutboundLogger.class);
	
	@ServiceActivator(inputChannel = "outboundChannel")
	public void log(GenericMessage<?> message) {
		try {
			LOG.info("Payload " + (message == null ? null : message.getPayload().getClass()));
			LOG.info("" + message);			
		} catch(Exception e) {
			LOG.error("", e);
		}
	}
}
