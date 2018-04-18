package at.ac.tuwien.dsg.sanalytics.bridge.stdio;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.stream.CharacterStreamReadingMessageSource;
import org.springframework.messaging.MessageChannel;

@Configuration
@Profile("inbound-stdin")
public class StdInboundConfig {

	@Bean
	public IntegrationFlow stdInIntegrationflow(
			@Qualifier("inboundChannel") MessageChannel channel) {
		return IntegrationFlows
				.from(CharacterStreamReadingMessageSource.stdin(),
						e -> e.poller(Pollers.fixedDelay(1000)))
//				.wireTap("wireTap")
				.channel(channel).get();
	}
}
