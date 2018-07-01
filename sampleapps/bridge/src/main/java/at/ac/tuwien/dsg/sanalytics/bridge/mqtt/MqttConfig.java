package at.ac.tuwien.dsg.sanalytics.bridge.mqtt;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;

import io.prometheus.client.Counter;

@Configuration
@Profile({"inbound-mqtt", "outbound-mqtt"})
public class MqttConfig {
	
	private Counter connectionLostCounter = Counter.build()
			.name("bridge_mqtt_connection_lost_total")
			.labelNames("cause")
			.help("The number of times the mqtt connection was lost")
			.register();
	
	@Bean
	public ApplicationListener<MqttConnectionFailedEvent> mqttConnectionFailedListener() {
		return new ApplicationListener<MqttConnectionFailedEvent>() {

			@Override
			public void onApplicationEvent(MqttConnectionFailedEvent event) {
				connectionLostCounter.labels(event.getCause()+"").inc();
			}
		};
	}
}
