package at.ac.tuwien.dsg.sanalytics.cep;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
@Profile({"default", "mqtt"})
public class MqttConfig {

	@Autowired
	private MessageChannel inputChannel;
	
	@Value("${mqtt.brokerURL:tcp://localhost:1883}")
	private String mqttBrokerURL = "tcp://localhost:1883";

	@Bean
	public MqttPahoMessageDrivenChannelAdapter inbound() {
		
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttBrokerURL,
				"cepApp_" + UUID.randomUUID().toString()
			, "sensor/+/randomcount"
		// ,"$SYS/broker/bytes/received"
		// ,"$SYS/broker/bytes/sent"
		// ,"monitoring/add/mqttTopic"
		);
		adapter.setCompletionTimeout(5000);
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(1);
		adapter.setOutputChannel(inputChannel);
		return adapter;
	}
}
