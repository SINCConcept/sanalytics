package at.ac.tuwien.dsg.sanalytics.sensor;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({"random", "dummy"})
public class RandomCounterSensor {
	
	@MessagingGateway(
		defaultRequestChannel = "mqttOutboundChannel", 
		defaultHeaders = @GatewayHeader(
				name = "mqtt_topic", 
				expression = "'sensor/' + @actualSensorName + '/randomcount'")
		)
	public interface RandomCounterMqttTopicGateway {
		void sendToMqtt(String data);
	}

	@Autowired
	private RandomCounterMqttTopicGateway gateway;
	
	private long count = 0;
	private Random rand = new Random();
	
	@Scheduled(fixedDelayString = "${sensor.randomCount.fixedRate:10000}")
	public void sendRandom() {
		//one could CEP on big increases (many > 8 in a timeframe) for example
		count+= rand.nextInt(10); 
		if (System.currentTimeMillis() % 7 == 0) {
			System.out.println("skipping send");
			return;
		}
		System.out.println("sending count " + count);
		gateway.sendToMqtt(count + "");
	}
}
