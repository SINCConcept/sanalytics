package at.ac.tuwien.dsg.sanalytics.bridge.stdio;

import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.sanalytics.events.Datapoint;

@Component("inputTransformer")
public class StringToDatapointTransformer implements GenericTransformer<String, Object>{

	@Override
	public Object transform(String source) {
		return Datapoint.from(source);
	}

}
