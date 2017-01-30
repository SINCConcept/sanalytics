package at.ac.tuwien.dsg.sanalytics.cep;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

@Configuration
@Profile({"default", "mqtt"})
public class EsperConfig {

	@Bean
	public EPServiceProvider cepServiceProvider() {
		com.espertech.esper.client.Configuration cepConfig = new com.espertech.esper.client.Configuration();
		cepConfig.addEventType("RandomCount", RandomCount.class.getName());
		EPServiceProvider provider = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
		return provider;
	}

	@Bean
	public EPAdministrator cepAdm() {
		EPAdministrator cepAdm = cepServiceProvider().getEPAdministrator();
		return cepAdm;
	}

	@Bean
	public EPRuntime cepRT() {
		return cepServiceProvider().getEPRuntime();
	}

	@ServiceActivator(inputChannel = "inputChannel")
	public void process(Message<?> message) {
		Metrics.MESSAGES.inc();
		System.out.println("message: " + message);
		Long cnt = Long.valueOf(message.getPayload().toString());
		cepRT().sendEvent(new RandomCount(cnt, message.getHeaders().getTimestamp()));
	}
}
