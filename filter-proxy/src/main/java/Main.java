

import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class Main {
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

//		Connector connector = new ServerConnector(server); 
		
//		SelectChannelConnector connector = new SelectChannelConnector();
//		connector.setPort(8080);
//		server.addConnector(connector);

		ProtectionDomain domain = Main.class.getProtectionDomain();
		URL location = domain.getCodeSource().getLocation();
		WebAppContext webapp = new WebAppContext();
		webapp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		webapp.setContextPath("/");
		webapp.setWar(location.toExternalForm());
//		webapp.addServlet("at.ac.tuwien.dsg.sanalytics.filterproxy.MetricsFilterProxyServlet", "/*");
		webapp.addServlet("at.ac.tuwien.dsg.sanalytics.filterproxy.MetricsServlet", "/metrics/*");
		server.setHandler(webapp);

		server.start();
		server.join();

	}
}
