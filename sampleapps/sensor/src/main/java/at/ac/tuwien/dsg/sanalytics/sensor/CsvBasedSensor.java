package at.ac.tuwien.dsg.sanalytics.sensor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("csv")
public class CsvBasedSensor {

	@MessagingGateway(
		defaultRequestChannel = "mqttOutboundChannel", 
		defaultHeaders = @GatewayHeader(
				name = "mqtt_topic", 
				expression = "'sensor/' + @actualSensorName + '/datapoint/' + #args[0] + '/'")
		)
	public interface CsvDataMqttGateway {
		void sendToMqtt(@Header("datapoint") String datapoint, @Payload String data);
	}
	
	private final static Logger LOG = LoggerFactory.getLogger(CsvBasedSensor.class);
	
	@Value("${csv.file:/data/sensor.csv}")
	private String fileName;
	
	@Autowired
	private SensorApp app;
	
	@Autowired
	private CsvDataMqttGateway gateway;

	@Scheduled(fixedDelay = 10_000)
	public void sendCSVDataToMqtt() throws FileNotFoundException, IOException {
		String sensorName = app.actualSensorName();
		LOG.info("starting to send new batch of data (sensorid = '" + sensorName + "')");
		File f = new File(fileName);
		try(FileInputStream fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis)) {
			DataReader dr = new DataReader(sensorName, bis);
			Entry<String, String> v = null;
			while((v = dr.nextValue(System.currentTimeMillis())) != null) {
				LOG.info("sending: " + v);
				gateway.sendToMqtt(v.getKey(), v.getValue());
			}
		}
	}
	
	
}
