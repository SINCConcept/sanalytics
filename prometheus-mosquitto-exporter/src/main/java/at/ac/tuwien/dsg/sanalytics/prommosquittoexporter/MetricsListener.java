package at.ac.tuwien.dsg.sanalytics.prommosquittoexporter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@Component
public class MetricsListener {
	

	private static Map<String, Type> namesToTypes = new HashMap<>();
	static {
		namesToTypes.put("mosquitto_SYS_broker_publish_bytes_received", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_load_messages_sent_1min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_messages_sent_5min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_messages_sent_15min", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_load_connections_1min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_connections_5min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_connections_15min", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_clients_connected", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_load_bytes_sent_1min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_bytes_sent_5min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_bytes_sent_15min", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_messages_received", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_load_bytes_received_1min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_bytes_received_5min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_bytes_received_15min", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_clients_maximum", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_bytes_received", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_messages_sent", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_clients_disconnected", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_bytes_sent", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_load_messages_received_1min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_messages_received_5min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_messages_received_15min", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_retained_messages_count", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_publish_messages_sent", Type.COUNTER);
		namesToTypes.put("mosquitto_SYS_broker_publish_messages_received", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_publish_messages_dropped", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_clients_total", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_load_publish_sent_1min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_publish_sent_5min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_publish_sent_15min", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_load_sockets_1min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_sockets_5min", Type.GAUGE);
		namesToTypes.put("mosquitto_SYS_broker_load_sockets_15min", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_clients_inactive", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_heap_maximum", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_subscriptions_count", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_clients_active", Type.GAUGE);
		
		namesToTypes.put("mosquitto_SYS_broker_publish_bytes_sent", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_heap_current", Type.GAUGE);	
		
		namesToTypes.put("mosquitto_SYS_broker_clients_expired", Type.COUNTER);
		
		namesToTypes.put("mosquitto_SYS_broker_messages_stored", Type.GAUGE);
	}
		
	private Type determineType(String sanKey) {
		Type t = namesToTypes.get(sanKey);
		return t == null ? Type.UNTYPED : t;
	}

	private String sanitizeMetricsName(String s) {
		return "mosquitto_" + Collector.sanitizeMetricName(s.replace("$", "")).replace("/", "_");
	}

	
	private static ConcurrentHashMap<String, MetricFamilySamples> metrics = new ConcurrentHashMap<>();

	@ServiceActivator(inputChannel = "inboundChannel")
	public void process(Message<?> message) {
		String key = message.getHeaders().get("mqtt_topic", String.class);
//		metrics.put(key)
		try {
			double payload = Double.valueOf(message.getPayload().toString());
			String sanKey = sanitizeMetricsName(key);
			metrics.compute(sanKey, (s, old) -> new MetricFamilySamples(
					s
					, determineType(sanKey)
					, ""
					, Collections.singletonList(createSample(sanKey, payload))));
		} catch(NumberFormatException e) {
			//empty
		}
		System.out.println(message);

	}

	private Sample createSample(String key, double payload) {
		
		return new Sample(key, Collections.emptyList(), Collections.emptyList(), payload);
	}

	public static Enumeration<MetricFamilySamples> metricFamilySamples() {
		return Collections.enumeration(metrics.values());
	}
}
