package at.ac.tuwien.dsg.sanalytics.bridge.stdio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.stream.CharacterStreamReadingMessageSource;
import org.springframework.integration.transformer.GenericTransformer;

import at.ac.tuwien.dsg.sanalytics.events.Datapoint;

@Configuration
@Profile("inbound-stdin")
public class StdInboundConfig {

	@Bean({ "identityInputTransformer", "inputTransformer" })
	@ConditionalOnMissingBean(name = "inputTransformer")
	public GenericTransformer<String, Object> identityInputTransformer() {
		return s -> s;
	}

	@Bean
	public IntegrationFlow stdInIntegrationflow(
			@Autowired @Qualifier("inputTransformer") GenericTransformer<String, Object> inputTransformer) {
		return IntegrationFlows
				.from(CharacterStreamReadingMessageSource.stdin(),
						e -> e.poller(Pollers.fixedDelay(1000)))
				.transform(inputTransformer).channel("inboundChannel").get();
	}
}
