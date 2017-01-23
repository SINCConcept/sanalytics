package at.ac.tuwien.dsg.sanalytics.sensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TimeSensor {
	
	@MessagingGateway(
		defaultRequestChannel = "mqttOutboundChannel", 
		defaultHeaders = @GatewayHeader(
				name = "mqtt_topic", 
				expression = "'sensor/' + @actualSensorName + '/time'")
		)
	public interface TimeMqttTopicGateway {
		void sendToMqtt(String data);
	}

	@Autowired
	private TimeMqttTopicGateway gateway;
	
	@Scheduled(fixedDelay = 10_000)
	public void sendTime() {
		gateway.sendToMqtt(System.currentTimeMillis()+"");
	}

}
