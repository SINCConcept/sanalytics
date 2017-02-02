package at.ac.tuwien.dsg.sanalytics.bridge.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableRabbit
@Profile("outbound-rabbitmq")
@ImportAutoConfiguration(RabbitAutoConfiguration.class)
public class RabbitMQOutboundConfig {

	@Value("${outbound-rabbitmq.queue:events}")
	private String queueName;
	
	@Bean
	Queue outboundQueue() {
		return new Queue(queueName, false);
	}
	
//	@Bean
//	public RabbitTemplate amqpTemplate(@Qualifier("outboundRabbitCF") ConnectionFactory connectionFactory) {
//		RabbitTemplate t = new RabbitTemplate();
//		t.setConnectionFactory(connectionFactory);
//		return t;
//	}
//	
//	@Value("${outbound-rabbitmq.host:localhost}")
//	private String host;
//	
//	@Bean
//	public RabbitConnectionFactoryBean rabbitConnectionFactoryBean() {
//		RabbitConnectionFactoryBean rcfb = new RabbitConnectionFactoryBean();
//		rcfb.setHost(host);
//		rcfb.setUsername("guest");
//		rcfb.setPassword("guest");
//		return rcfb;
//	}

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
