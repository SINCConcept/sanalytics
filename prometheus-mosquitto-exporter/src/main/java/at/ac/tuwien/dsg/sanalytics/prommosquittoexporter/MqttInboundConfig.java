package at.ac.tuwien.dsg.sanalytics.prommosquittoexporter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttInboundConfig {
	
	private final static Logger LOG = LoggerFactory.getLogger(MqttInboundConfig.class);
	
	@Value("${inbound-mqtt.brokerURL:tcp://localhost:1883}")
	private String brokerURL = "tcp://localhost:1883";

	@Value("${inbound-mqtt.subscriptions:$SYS/#}")
	private List<String> subscriptions = new ArrayList<>();
	
	@Autowired
	private MessageChannel inboundChannel;

	@Bean
	public MqttPahoMessageDrivenChannelAdapter inbound() {
		
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
				brokerURL,
				"prometheus-mosquitto-exporter"
//			, subscriptions
		// ,"$SYS/broker/bytes/received"
		// ,"$SYS/broker/bytes/sent"
		// ,"monitoring/add/mqttTopic"
		);
		LOG.info("subscriptions: " + subscriptions);
		for(String t : subscriptions)
			adapter.addTopic(t);
		
		adapter.setCompletionTimeout(5000);
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(1);
		adapter.setOutputChannel(inboundChannel);
		return adapter;
	}
	
}
