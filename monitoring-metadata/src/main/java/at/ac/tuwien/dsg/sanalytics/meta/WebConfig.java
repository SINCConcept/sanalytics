package at.ac.tuwien.dsg.sanalytics.meta;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}
	
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		// so spring doesn't try to resolve the Content-Type from the path
		// if a produces-annotation-attribute is present on the @RequestMapping
		// annotation. 
		configurer.favorPathExtension(false);
	}
}
