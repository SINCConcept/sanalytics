package at.ac.tuwien.dsg.sanalytics.bridge;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import io.prometheus.client.Counter;

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
	
	private Counter bridgedMessages = Counter.build()
			.name("forwarder_bridged_messages_total")
			.labelNames("payloadClass")
			.help("The number of messages that have been bridged between input and output")
			.register();
	
	@ServiceActivator(inputChannel = "inboundChannel", outputChannel = "outboundChannel")
	public Message<?> bridge(Message<?> m) {
		String payloadClass = m.getPayload() == null ? null : m.getPayload().getClass().getName();
		bridgedMessages.labels(payloadClass).inc();
		return m;
	}
	
	//gary russel suggested this, i need to find out how to increase the counter though (wiretap?)
//	@Bean
//	@ServiceActivator(inputChannel="inboundChannel")
//	public MessageHandler bridge() {
//	    BridgeHandler handler = new BridgeHandler();
//	    handler.setOutputChannelName("outboundChannel");
//	    return handler;
//	}
	
//	@Bean
//	@BridgeFrom("inboundChannel")
//	public BridgeHandler bridgeHandler() {
//		BridgeHandler bh = new BridgeHandler();
//		bh.setOutputChannelName("outboundChannel");
//		return bh;
//	}
}
