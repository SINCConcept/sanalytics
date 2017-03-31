package at.ac.tuwien.dsg.sanalytics.bridge.esper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import at.ac.tuwien.dsg.sanalytics.events.Datapoint;
import at.ac.tuwien.dsg.sanalytics.events.RandomCount;

@Configuration
@Profile({"esper-cep-randomcount", "esper-cep-datapoint"})
public class EsperConfig {

	@Bean
	public EPServiceProvider cepServiceProvider() {
		com.espertech.esper.client.Configuration cepConfig = new com.espertech.esper.client.Configuration();
		cepConfig.addEventType("RandomCount", RandomCount.class.getName());
		cepConfig.addEventType("Datapoint", Datapoint.class.getName());
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
}