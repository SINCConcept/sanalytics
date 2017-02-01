package at.ac.tuwien.dsg.sanalytics.bridge;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * simple bridge that just forwards messages 
 * 
 * @author cproinger
 *
 */
@MessageEndpoint
@Profile("forwarder")
public class ForwardingBridge {

//	@Bean
//	public IntegrationFlow bridge() {
//		return f -> f.bridge(c -> c.)
//	}
	
//	@Bean
//	@BridgeFrom("inboundChannel")
//	@BridgeTo("outboundChannel")
//	public MessageChannel bridge() {
//		return new DirectChannel();
//	}
	
	@ServiceActivator(inputChannel = "inboundChannel", outputChannel = "outboundChannel")
	public Message<?> bridge(Message<?> m) {
		return m;
	}
	
//	@Bean
//	@BridgeFrom("inboundChannel")
//	public BridgeHandler bridgeHandler() {
//		BridgeHandler bh = new BridgeHandler();
//		bh.setOutputChannelName("outboundChannel");
//		return bh;
//	}
}
