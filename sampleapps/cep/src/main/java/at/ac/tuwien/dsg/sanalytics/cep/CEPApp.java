package at.ac.tuwien.dsg.sanalytics.cep;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;

@SpringBootApplication
public class CEPApp {


	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(CEPApp.class)
//				.web(false)
				.run(args);
		//
		// System.out.println("press ENTER to exit");
		// try(Scanner sc = new Scanner(System.in)) {
		// sc.nextLine();
		// }
		// ctx.close();

	}
	
	@Bean
	public ServletRegistrationBean servletRegistrationBean(){
		DefaultExports.initialize();
	    return new ServletRegistrationBean(new MetricsServlet(),"/metrics", "/metrics/");
	}

	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}
	
	@Value("${mqtt.brokerURL:tcp://localhost:1883}")
	private String mqttBrokerURL = "tcp://localhost:1883";

	@Bean
	public MqttPahoMessageDrivenChannelAdapter inbound() {
		
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttBrokerURL,
				"cepApp_" + UUID.randomUUID().toString()
			, "sensor/+/randomcount"
		// ,"$SYS/broker/bytes/received"
		// ,"$SYS/broker/bytes/sent"
		// ,"monitoring/add/mqttTopic"
		);
		adapter.setCompletionTimeout(5000);
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(1);
		adapter.setOutputChannel(mqttInputChannel());
		return adapter;
	}
	
	@Bean
	public EPServiceProvider cepServiceProvider() {
		Configuration cepConfig = new Configuration();
		cepConfig.addEventType("RandomCount", RandomCount.class.getName());
		EPServiceProvider provider = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
		return provider;
	}
	
	@Bean 
	public EPAdministrator cepAdm() {
		EPAdministrator cepAdm = cepServiceProvider().getEPAdministrator();
		return cepAdm;
	}
	
	@Bean
	public EPRuntime cepRT() {
		return cepServiceProvider().getEPRuntime();
	}
	
	static final Counter messages = Counter.build()
			.name("messages_total").help("Total number of messages received via mqtt")
			.register();
	
	@ServiceActivator(inputChannel = "mqttInputChannel")
	public void process(Message<?> message) {
		messages.inc();
		System.out.println("message: " + message);
		Long cnt = Long.valueOf(message.getPayload().toString());
		cepRT().sendEvent(new RandomCount(cnt, message.getHeaders().getTimestamp()));
	}
}
