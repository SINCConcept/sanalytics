package at.ac.tuwien.dsg.sanalytics.bridge.sql;

import java.sql.SQLException;

import org.h2.tools.Server;

public class SqlDatapointCEPResultOutboundInterceptorDemo {

	public static void main(String[] args) throws SQLException {
		new Server().runTool();
	}
}
