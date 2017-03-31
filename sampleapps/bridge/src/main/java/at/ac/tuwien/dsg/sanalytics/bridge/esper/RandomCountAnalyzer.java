package at.ac.tuwien.dsg.sanalytics.bridge.esper;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import at.ac.tuwien.dsg.sanalytics.events.RandomCount;

@Component
@Profile({"esper-cep-randomcount"})
public class RandomCountAnalyzer extends AbstractAnalyzer {

	@MessagingGateway(
			defaultRequestChannel = "outboundChannel"
//			defaultHeaders = @GatewayHeader(
//					name = "mqtt_topic", 
//					expression = "'sensor/' + @actualSensorName + '/randomcount'")
			)
	@Profile({"esper-cep-randomcount"})
	public interface RandomCounterGateway {
		void sendEvent(RandomCount rc);
	}
	
	private final static Logger LOG = LoggerFactory.getLogger(RandomCountAnalyzer.class);
	
	@Autowired
	private RandomCounterGateway rcGateway;
	
	@Autowired
	private EPAdministrator cepAdm;
	
	@Autowired
	private EPRuntime cepRT;

	@PostConstruct
	private void initStatement() {
		EPStatement stmt = cepAdm.createEPL("select * from RandomCount.win:length(3) having max(count) - min(count) > 15");
		stmt.addListener(new UpdateListener() {
			
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				LOG.info("new Events: " + Arrays.toString(newEvents));
				LOG.info("old Events: " + Arrays.toString(oldEvents));
				for(EventBean eb : newEvents) {
					RandomCount rc = (RandomCount) eb.getUnderlying();
					//rcRepo.save(rc);
					rcGateway.sendEvent(rc);
					eventsSent.inc();
				}
			}
		});
	}

	@ServiceActivator(inputChannel = "inboundChannel")
	public void process(Message<?> message) {
		messages.inc();
		LOG.info("message: " + message);
		Long cnt = Long.valueOf(message.getPayload().toString());
		cepRT.sendEvent(new RandomCount(cnt, message.getHeaders().getTimestamp()));
	}
}
