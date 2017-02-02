package at.ac.tuwien.dsg.sanalytics.cep;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.sanalytics.events.RandomCount;

@Component
@Profile("rabbitmq")
public class SimpleEventPersister {
	
	@Autowired
	private RandomCountRepository rcRepo;
	
	@ServiceActivator(inputChannel="inputChannel")
	public void saveEvent(GenericMessage<?> message) {
		try {
			String s = new String((byte[]) message.getPayload());
			long cnt = Long.valueOf(s);
			RandomCount rc = rcRepo.save(new RandomCount(cnt, message.getHeaders().getTimestamp()));
			System.out.println("saved " + rc);
			Metrics.EVENTS_SAVED.inc();
		} catch (NumberFormatException e) {
			System.out.println("payload=" + message.getPayload().toString());
			e.printStackTrace();
		}
	}
}
