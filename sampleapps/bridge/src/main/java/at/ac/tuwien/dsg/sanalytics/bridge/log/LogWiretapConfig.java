package at.ac.tuwien.dsg.sanalytics.bridge.log;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class LogWiretapConfig {

	@Bean
	@ConditionalOnMissingBean(name = "wireTap")
	public IntegrationFlow wireTap() {
	    return f -> f.handle(p -> {});
	}
}
