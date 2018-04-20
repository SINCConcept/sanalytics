package at.ac.tuwien.dsg.sanalytics.bridge.esper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.common.collect.Maps;

import at.ac.tuwien.dsg.sanalytics.events.Datapoint;
import at.ac.tuwien.dsg.sanalytics.events.DatapointCEPResult;

@Component
@Profile("esper-cep-datapoint")
public class DatapointAnalyzer extends AbstractAnalyzer {

	@MessagingGateway(
			defaultRequestChannel = "outboundChannel"
//			defaultHeaders = @GatewayHeader(
//					name = "mqtt_topic", 
//					expression = "'sensor/' + @actualSensorName + '/randomcount'")
			)
	@Profile("esper-cep-datapoint")
	public interface DatapointCEPResultGateway {
		void sendEvent(Datapoint datapointCEPResult);
	}
	
	private final static Logger LOG = LoggerFactory.getLogger(DatapointAnalyzer.class);
	
	private static final String DEFAULT_ESPER_SELECT 
	= "select station, datapoint, avg(value) as value "
			+ "from Datapoint.win:time_batch(60 sec) "
			+ "group by station, datapoint "
			+ "having avg(value) is not null";

	private DatapointCEPResultGateway datapointGateway;
	
	private EPAdministrator cepAdm;
	
	private EPRuntime cepRT;

	
	private String esperSelect;

	@Autowired
	public DatapointAnalyzer(DatapointCEPResultGateway datapointGateway, 
			EPAdministrator cepAdm, 
			EPRuntime cepRT, 
			@Value("${esper.datapoint.query:}") String esperSelect) throws IOException {
		this.datapointGateway = datapointGateway;
		this.cepAdm = cepAdm;
		this.cepRT = cepRT;
		this.esperSelect = StringUtils.isEmpty(esperSelect) 
				? DEFAULT_ESPER_SELECT
				: esperSelect;
//				? Resources.toString(Resources.getResource("/query.epl"), Charsets.UTF_8) 
//				: Resources.toString(Resources.getResource(esperSelect), Charsets.UTF_8);
	}
	
	@PostConstruct
	private void initStatement() {
		EPStatement stmt = cepAdm.createEPL(esperSelect);
		stmt.addListener(new UpdateListener() {
			
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				LOG.info("new Events: " + Arrays.toString(newEvents));
				LOG.info("old Events: " + Arrays.toString(oldEvents));
				for(EventBean eb : newEvents) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> m = Maps.newHashMap((Map<String, Object>) eb.getUnderlying());
					if(m.size() == 0) {
						continue;
					}
					datapointGateway.sendEvent(new Datapoint(m));
					eventsSent.inc();
				}
			}
		});
	}

	@ServiceActivator(inputChannel = "inboundChannel")
	public void process(Message<?> message) {
		messages.inc();
		LOG.debug("message: " + message);
		final Datapoint d;
		if (message.getPayload() instanceof Datapoint) {
			d = (Datapoint) message.getPayload();
		} else {
			String sPayload = message.getPayload().toString();
			Double v = Double.valueOf(sPayload);
			// something like /sensor/<station_id>/datapoint/<datapoint_id>/ 
			String topic = (String) message.getHeaders().get("mqtt_topic");
			d = Datapoint.from(topic, v);
		}
		cepRT.sendEvent(d);
	}
}
