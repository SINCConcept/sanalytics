package at.ac.tuwien.dsg.sanalytics.mqttclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@SpringBootApplication
public class MqttClient {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(MqttClient.class)
				.web(false).run(args);
		//
		// System.out.println("press ENTER to exit");
		// try(Scanner sc = new Scanner(System.in)) {
		// sc.nextLine();
		// }
		// ctx.close();

	}

	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}
	
	@Value("${mqtt.brokerURL:tcp://localhost:1883}")
	private String mqttBrokerURL = "tcp://localhost:1883";

	@Bean
	public MqttPahoMessageDrivenChannelAdapter inbound() {
		
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttBrokerURL,
				"mqttClient_" + UUID.randomUUID().toString()
			, "sensor/#"
		// ,"$SYS/broker/bytes/received"
		// ,"$SYS/broker/bytes/sent"
		// ,"monitoring/add/mqttTopic"
		);
		adapter.setCompletionTimeout(5000);
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(1);
		adapter.setOutputChannel(mqttInputChannel());
		return adapter;
	}

//	@Router(inputChannel = "mqttInputChannel")
//	public String route(Message<?> message) {
//		String topic = message.getHeaders().get("mqtt_topic").toString();
//		return "channel--" + topic.replaceAll("[\\$/]", "_");
//	}

	@ServiceActivator(inputChannel = "mqttInputChannel")
	public void process(Message<?> message) {
		System.out.println(message);
	}
}
