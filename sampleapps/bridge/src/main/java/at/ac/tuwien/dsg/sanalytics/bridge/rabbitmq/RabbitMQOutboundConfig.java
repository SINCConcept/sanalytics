package at.ac.tuwien.dsg.sanalytics.bridge.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
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
@Profile("outbound-rabbitmq")
public class RabbitMQOutboundConfig {

	@Value("${outbound-rabbitmq.queue:events}")
	private String queueName;
	
	@Bean
	Queue outboundQueue() {
		return new Queue(queueName, false);
	}

	@Bean
	public IntegrationFlow outboundRabbitMQFlow(
			@Qualifier("outboundChannel") MessageChannel outboundChannel, 
			AmqpTemplate amqpTemplate) {

		return IntegrationFlows
				.from(outboundChannel)				
				.handle(Amqp.outboundAdapter(amqpTemplate).routingKey(queueName))
				.get();
	}
}
