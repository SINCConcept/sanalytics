package at.ac.tuwien.dsg.sanalytics.bridge.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageHandler;

import at.ac.tuwien.dsg.sanalytics.bridge.AppId;

@Configuration
@Profile("outbound-mqtt")
public class MqttOutboundConfig {
	
	@Value("${outbound-mqtt.brokerURL:tcp://localhost:1883}")
	private String brokerURL = "tcp://localhost:1883";

	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		System.out.println("mqttBrokerURL = " + brokerURL);
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		factory.setServerURIs(brokerURL);
		// factory.setUserName("username");
		// factory.setPassword("password");
		return factory;
	}
	
	@Bean
	@ServiceActivator(inputChannel = "outboundChannel")
	public MessageHandler mqttOutbound() {
		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
				AppId.APP_ID, mqttClientFactory());
		messageHandler.setAsync(true);
		
		//messageHandler.setDefaultTopic("sensor/" + actualSensorName + "/time");
		return messageHandler;
	}
}
