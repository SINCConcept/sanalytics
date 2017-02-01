package at.ac.tuwien.dsg.sanalytics.bridge;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;

/**
 * Every bridge is made up of
 * <ul>
 * <li>some service endpoint(HTTP/REST/SOAP/..) or inboundAdapter
 * (JMS/MQTT/AMQP)</li>
 * <li>the inboundChannel in which messages are routed</li>
 * <li>an outboundChannel and some stuff between inbound and outboundChannel
 * </li>
 * <li>an outboundAdapter(JMS/MQTT/AMQP/HTTP/REST/SOAP/MongoDB/...).
 * </ul>
 * 
 * how the bridge is made up is defined via activating profiles.
 * 
 * @author cproinger
 *
 */
@Configuration
@ComponentScan
@EnableSpringDataWebSupport
@EnableWebMvc
@EnableIntegration
@IntegrationComponentScan
public class BridgeApp {
	
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BridgeApp.class).run(args);
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainerFactory() {
		TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
		return factory;
	}

	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		DefaultExports.initialize();
		return new ServletRegistrationBean(new MetricsServlet(), "/metrics", "/metrics/");
	}

	/**
	 * every bridge has one inboundChannel
	 */
	@Bean
	public MessageChannel inboundChannel() {
		return new DirectChannel();
	}

	/**
	 * every bridge has one outboundChannel
	 */
	@Bean
	public MessageChannel outboundChannel() {
		return new DirectChannel();
	}
	
	
}
