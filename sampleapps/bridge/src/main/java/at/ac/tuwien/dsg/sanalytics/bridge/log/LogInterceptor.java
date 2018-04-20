package at.ac.tuwien.dsg.sanalytics.bridge.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Configuration
public class LogInterceptor  {
	
	private final static Logger LOG = LoggerFactory.getLogger(LogInterceptor.class);
	
	@Bean
	@ConditionalOnMissingBean(name = "outboundInterceptor")
	public ChannelInterceptor outboundInterceptor() {
		return new ChannelInterceptorAdapter() {
			@Override
			public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
				LOG.debug("interceptor: " + message);
			}
		};
	}
	
	@Bean
	@ConditionalOnMissingBean(name = "inboundInterceptor")
	public ChannelInterceptor inboundInterceptor() {
		return new ChannelInterceptorAdapter() {
			@Override
			public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
				LOG.debug("interceptor: " + message);
			}
		};
	}
}
