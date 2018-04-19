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

	/**
	 * every bridge has one inboundChannel
	 */
	@Bean
	public MessageChannel inboundChannel() {
		return new DirectChannel();
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
	
	@Autowired
	@Qualifier("outboundInterceptor")
	private ChannelInterceptor outboundInterceptor;
	
	@Bean
	public MessageChannel wireTapChannel() {
		return new DirectChannel();
	}
}
