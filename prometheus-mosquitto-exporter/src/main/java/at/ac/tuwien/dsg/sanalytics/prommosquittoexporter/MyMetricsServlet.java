package at.ac.tuwien.dsg.sanalytics.prommosquittoexporter;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.prometheus.client.exporter.common.TextFormat;

/**
 * copied and modified from io.prometheus.client.exporter.MetricsServlet
 * 
 * @author cproinger
 *
 */
public class MyMetricsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public MyMetricsServlet() {
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(TextFormat.CONTENT_TYPE_004);

		Writer writer = resp.getWriter();
		TextFormat.write004(writer, MetricsListener.metricFamilySamples());
		writer.flush();
		writer.close();
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

}
