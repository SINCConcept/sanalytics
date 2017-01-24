package at.ac.tuwien.dsg.sanalytics.sensor;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@IntegrationComponentScan
@EnableScheduling
public class SensorApp {
	
	public static void main(String[] args) throws InterruptedException {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(SensorApp.class)
				.web(false)
				.run(args);
	}

	@Value("${mqtt.brokerURL:tcp://localhost:1883}")
	private String mqttBrokerURL = "tcp://localhost:1883";

	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		System.out.println("mqttBrokerURL = " + mqttBrokerURL);
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		factory.setServerURIs(mqttBrokerURL);
		// factory.setUserName("username");
		// factory.setPassword("password");
		return factory;
	}
	
	@Value("${sensor.name:}")
	private String providedSensorName;

	@Bean
	String actualSensorName() {
		String actualSensorName;
		if(providedSensorName == null || providedSensorName.isEmpty())
			actualSensorName = UUID.randomUUID().toString();
		else
			actualSensorName = providedSensorName;
		return actualSensorName;
	}
	
	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutbound() {
		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("sensor_" + actualSensorName(), mqttClientFactory());
		messageHandler.setAsync(true);
		
		//messageHandler.setDefaultTopic("sensor/" + actualSensorName + "/time");
		return messageHandler;
	}

	@Bean
	public MessageChannel mqttOutboundChannel() {
		return new DirectChannel();
	}
}
