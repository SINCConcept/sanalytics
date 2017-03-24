package at.ac.tuwien.dsg.sanalytics.sensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({"time", "dummy"})
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
	
	@Scheduled(fixedRateString = "${sensor.time.fixedRate:10000}")
	public void sendTime() {
		gateway.sendToMqtt(System.currentTimeMillis()+"");
	}

}
