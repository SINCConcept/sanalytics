package at.ac.tuwien.dsg.sanalytics.bridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.resourceresolver.SpringResourceResourceResolver;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

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
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BridgeApp.class)
				.run(args);
	}

	// why is this needed???
	@Bean
	public EmbeddedServletContainerFactory servletContainerFactory() {
		TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
		factory.setPort(Integer.valueOf(System.getProperty("server.port", "8080")));
		return factory;
	}

	@Bean
	public ServletRegistrationBean metricsServletRegistrationBean() {
		DefaultExports.initialize();
		ServletRegistrationBean srb = new ServletRegistrationBean(new MetricsServlet(), "/metrics",
				"/metrics/");
		return srb;
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
