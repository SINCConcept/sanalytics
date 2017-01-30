package at.ac.tuwien.dsg.sanalytics.cep;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@Configuration
@Profile("rabbitmq")
public class RabbitMQConfig {

	@Bean
    Queue queue() {
        return new Queue("events", false);
    }
	
	@Bean
	public IntegrationFlow inbound(
			ConnectionFactory connectionFactory, 
			@Qualifier("inputChannel") MessageChannel channel) {
		
		return IntegrationFlows.from(Amqp.inboundAdapter(connectionFactory, "events"))
			.channel(channel)
			.get();
	}

}
