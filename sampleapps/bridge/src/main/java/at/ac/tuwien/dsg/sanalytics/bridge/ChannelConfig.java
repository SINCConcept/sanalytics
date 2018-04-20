package at.ac.tuwien.dsg.sanalytics.bridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

@Configuration
public class ChannelConfig {

	@Autowired
	@Qualifier("inboundInterceptor")
	private ChannelInterceptor inboundInterceptor;

	@Autowired
	@Qualifier("outboundInterceptor")
	private ChannelInterceptor outboundInterceptor;
	
	/**
	 * every bridge has one inboundChannel
	 */
	@Bean
	public MessageChannel inboundChannel() {
		DirectChannel channel = new DirectChannel();
		channel.addInterceptor(inboundInterceptor);
		return channel;
	}
	
	
	/**
	 * every bridge has one outboundChannel
	 */
	@Bean
	public MessageChannel outboundChannel() {
		DirectChannel channel = new DirectChannel();
		channel.addInterceptor(outboundInterceptor);
		return channel;
	}
	
	@Bean
	public MessageChannel wireTapChannel() {
		return new DirectChannel();
	}
}
