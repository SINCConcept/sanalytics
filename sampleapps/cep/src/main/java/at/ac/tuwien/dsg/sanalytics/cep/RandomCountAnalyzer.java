package at.ac.tuwien.dsg.sanalytics.cep;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import at.ac.tuwien.dsg.sanalytics.events.RandomCount;

@Component
@Profile({"default", "mqtt"})
public class RandomCountAnalyzer {

	@Autowired
	private EPAdministrator cepAdm;
	
	@Autowired
	private RandomCountRepository rcRepo;
	
	@PostConstruct
	private void initStatement() {
		EPStatement stmt = cepAdm.createEPL("select * from RandomCount.win:length(3) having max(count) - min(count) > 15");
		stmt.addListener(new UpdateListener() {
			
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				System.out.println("new Events: " + Arrays.toString(newEvents));
				System.out.println("old Events: " + Arrays.toString(oldEvents));
				for(EventBean eb : newEvents) {
					RandomCount rc = (RandomCount) eb.getUnderlying();
					rcRepo.save(rc);
					Metrics.EVENTS_SAVED.inc();
				}
			}
		});
	}
}
