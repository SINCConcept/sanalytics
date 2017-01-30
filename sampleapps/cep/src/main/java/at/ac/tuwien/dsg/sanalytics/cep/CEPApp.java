package at.ac.tuwien.dsg.sanalytics.cep;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;

@SpringBootApplication
@IntegrationComponentScan
public class CEPApp {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(CEPApp.class)
				.run(args);
	}
	
	@Bean
	public ServletRegistrationBean servletRegistrationBean(){
		DefaultExports.initialize();
	    return new ServletRegistrationBean(new MetricsServlet(),"/metrics", "/metrics/");
	}

	@Bean
	public MessageChannel inputChannel() {
		return new DirectChannel();
	}

}
