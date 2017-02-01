package at.ac.tuwien.dsg.sanalytics.bridge.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableRabbit
@Profile("inbound-rabbitmq")
public class RabbitMQInboundConfig {

	@Value("${inbound-rabbitmq.queue:events}")
	private String queueName;
	
	@Bean
	Queue inboundQueue() {
		return new Queue(queueName, false);
	}

	@Bean
	public IntegrationFlow inboundRabbitMQFlow(ConnectionFactory connectionFactory,
			@Qualifier("inboundChannel") MessageChannel channel) {

		return IntegrationFlows
				.from(Amqp.inboundAdapter(connectionFactory, queueName)).
				channel(channel)
				.get();
	}

}
