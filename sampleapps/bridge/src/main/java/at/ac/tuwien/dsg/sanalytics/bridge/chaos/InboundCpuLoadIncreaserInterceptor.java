package at.ac.tuwien.dsg.sanalytics.bridge.chaos;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

import io.prometheus.client.Summary;

@Component("inboundInterceptor")
@Profile("chaos-cpuincrease-interceptor")
public class InboundCpuLoadIncreaserInterceptor extends ChannelInterceptorAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(InboundCpuLoadIncreaserInterceptor.class);
	
	private AtomicLong loopSize = new AtomicLong();
	private ExecutorService pool;

	@Value("${inboundCpuThrasherInterceptor.incrementAmount:1000}")
	private long incrementAmount;

	private Timer timer;
	
	private Summary interceptSummary = Summary.build()
			.subsystem("chaos")
			.name("inbound_cpuincrease_intercepts_summary")
			.help("Summary for the invocations of InboundCpuLoadIncreaserInterceptor")
			.quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
            .quantile(0.9, 0.01)   // Add 90th percentile with 1% tolerated error
            .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
			.register();
	
	@PostConstruct
	public void init() {
		pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
		timer = new Timer(1000, e -> doStuff());
		timer.start();;
	}
	
	@PreDestroy
	public void destroy() {
		timer.stop();
	}

	@Override
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		loopSize.addAndGet(incrementAmount);
	}

	// i don't wanna add another spring-dependency for that so i just use a timer. 
//	@Scheduled(fixedRate = 1000)
	public void doStuff() {
		int numCore = Runtime.getRuntime().availableProcessors();
		int numThreadsPerCore = 2;
		
		long ls = loopSize.get();
		LOG.info("submitting with loopSize: " + ls);
		for (int thread = 0; thread < numCore * numThreadsPerCore; thread++) {
			pool.submit(new BusyThread(ls));
		}
	}

	private class BusyThread implements Runnable {
		private long times;

		public BusyThread(long times) {
			this.times = times;
		}

		/**
		 * Generates the load when run
		 */
		@Override
		public void run() {
			// Loop for the given duration
			long start = System.currentTimeMillis();
			long sum = 0;
			for (long l = 0; l < times; l++) {
				sum += l;
			}
			long duration = System.currentTimeMillis() - start;
			if (duration > 1000) {
				LOG.info("BusyThread takes more than 1s, took(ms): " + duration);
			}
			interceptSummary.observe(duration);
		}
	}
}
