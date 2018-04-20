package at.ac.tuwien.dsg.sanalytics.bridge.chaos;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

import io.prometheus.client.Summary;

/**
 * use '-XX:+ExitOnOutOfMemoryError' with this
 */
@Component("inboundInterceptor")
@Profile("chaos-memleak-interceptor")
public class InboundMemoryLeakingInterceptor extends ChannelInterceptorAdapter {

	private List<byte[]> leakBytes = Collections.synchronizedList(new LinkedList<>());

	private Summary interceptSummary = Summary.build().subsystem("chaos")
			.name("inbound_memleak_intercepts_summary")
			.help("Summary for the invocations of InboundMemoryLeakingInterceptor")
			.quantile(0.5, 0.05) // Add 50th percentile (= median) with 5%
									// tolerated error
			.quantile(0.9, 0.01) // Add 90th percentile with 1% tolerated error
			.quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated
									// error
			.register();

	@Value("${inboundMemoryLeakingInterceptor.leakByteMaxSize:100}")
	private int leakByteMaxSize;

	@Value("${inboundMemoryLeakingInterceptor.leakByteMinSize:10}")
	private int leakByteMinSize;

	private Random random = new Random();

	@Override
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		final int size;
		size = leakByteMinSize >= leakByteMaxSize ? leakByteMinSize
				: leakByteMinSize + random.nextInt(leakByteMaxSize - leakByteMinSize);
		leakBytes.add(new byte[size]);
		interceptSummary.observe(size);
	}
}
