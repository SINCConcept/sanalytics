package at.ac.tuwien.dsg.sanalytics.meta;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves various scripts
 */
@RestController
@RequestMapping(path = "/platform")
public class PlatformSetupController {

	@RequestMapping(path = "/prometheus-platform.yml", method = RequestMethod.GET,
			produces = "text/plain")
	public ResponseEntity<String> getPrometheusPlatformConfig() throws IOException {
		try (InputStream is = getClass()
				.getResourceAsStream("/configfiles/prometheus/prometheus-platform.yml")) {
			String body = StreamUtils.copyToString(is, Charset.forName("UTF-8"));
			return new ResponseEntity<String>(body, HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/prometheus-platform.rules", method = RequestMethod.GET,
			produces = "text/plain")
	public ResponseEntity<String> getPrometheusPlatformRules() throws IOException {
		try (InputStream is = getClass()
				.getResourceAsStream("/configfiles/prometheus/platform.rules")) {
			String body = StreamUtils.copyToString(is, Charset.forName("UTF-8"));
			return new ResponseEntity<String>(body, HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/setup-platform.sh", method = RequestMethod.GET,
			produces = "text/plain")
	public ResponseEntity<String> getSetupPlatformScript(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();

		sb.append("# setup networks").append('\n');
		sb.append("echo 'SANALYTICS: setting up networks (expect errors if these already exist)'").append('\n');
		sb.append("docker network create -d overlay custom_monitoring").append('\n');
		sb.append("docker network create -d overlay platform_overlay").append('\n');
		// sb.append("mkdir -p /home/" + dockerUser +
		// "/myvolumes/prom/platform").append('\n');

		// cadvisor
		sb.append("# setup cadvisor").append('\n');
		sb.append("echo 'SANALYTICS: setting up cadvisor'").append('\n');
		sb.append("docker service create " + "--network=custom_monitoring "
				+ "--network=platform_overlay " + "-p 8888:8080 " + "--mode global "
				+ "--name cadvisor " + "--mount type=bind,source=/,target=/rootfs,readonly=true "
				+ "--mount type=bind,source=/var/run,target=/var/run,readonly=false "
				+ "--mount type=bind,source=/sys,target=/sys,readonly=true "
				+ "--mount type=bind,source=/var/lib/docker/,target=/var/lib/docker,readonly=true "
				+ "google/cadvisor").append('\n');

		sb.append("# setup prometheus").append('\n');
		sb.append("echo 'SANALYTICS: setting up prometheus'").append('\n');
		sb.append("mkdir -p ./platform/prometheus/").append('\n');
		sb.append("mkdir -p ./platform/prometheus/conf").append('\n');
		sb.append("mkdir -p ./platform/prometheus/data").append('\n');

		String baseUrl = getBaseUrl(req);
		sb.append("curl -sSL " + baseUrl + "platform/prometheus-platform.yml "
				+ "> ./platform/prometheus/conf/prometheus-platform.yml").append('\n');

		sb.append("curl -sSL " + baseUrl + "platform/prometheus-platform.rules "
				+ "> ./platform/prometheus/conf/platform.rules").append('\n');

		String platformPrometheus = "${PWD}/platform/prometheus/";
		String confDir = platformPrometheus + "conf";
		sb.append("docker service create -p 9090:9090 " + "--mount type=bind,source=" + confDir
				+ ",target=/etc/prometheus/,readonly "
				+ "--mount type=bind,source=" + platformPrometheus + "data,target=/prometheus/ "
				+ "--name=prom_platform " + "--network=custom_monitoring "
				+ "--network=platform_overlay " + "prom/prometheus:v1.5.3 "
				+ "-config.file=/etc/prometheus/prometheus-platform.yml "
				+ "-storage.local.path=/prometheus "
				+ "-web.console.libraries=/etc/prometheus/console_libraries "
				+ "-web.console.templates=/etc/prometheus/consoles").append('\n');

		String body = sb.toString();
		return new ResponseEntity<String>(body, HttpStatus.OK);
	}
	
	public static String getBaseUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		String uri = req.getRequestURI();
		String ctx = req.getContextPath();
		String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
		return base;
	}
}
