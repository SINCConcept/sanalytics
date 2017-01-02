package at.ac.tuwien.dsg.sanalytics.filterproxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.verify.VerificationTimes;

import io.prometheus.client.Metrics.MetricFamily;

import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;

@RunWith(MockitoJUnitRunner.class)
public class MetricsServletTest {

	@Mock
	private HttpServletRequest req;
	
	@Mock
	private HttpServletResponse resp;
	
	@Rule
	public MockServerRule cadvisor1 = new MockServerRule(this);
	@Rule
	public MockServerRule cadvisor2 = new MockServerRule(this);

	private ByteArrayOutputStream baos;

	private MockServerClient cadvisor1client;

	private MockServerClient cadvisor2client;
	
	@Before
	public void setupRequest() throws IOException {
		baos = new ByteArrayOutputStream();
		ServletOutputStream sos = new ServletOutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				baos.write(b);
			}
		};
		when(resp.getOutputStream()).thenReturn(sos);
		
		
		cadvisor1client = new MockServerClient("localhost", cadvisor1.getPort());
		cadvisor2client = new MockServerClient("localhost", cadvisor2.getPort());
	}
	
	@Test
	public void testDoGet_slice0() throws ServletException, IOException {
		MetricsResponse response = verifyDoGet("/metrics/slice0");
		assertEquals(38, response.getMetricsFamilies().size());
	}
	
	@Test
	public void testDoGet_sliceDoesNotExist() throws ServletException, IOException {
		MetricsResponse response = verifyDoGet("/metrics/sliceDoesNotExist");
		assertEquals(0, response.getMetricsFamilies().size());
	}
	
	public MetricsResponse verifyDoGet(String uri) throws ServletException, IOException {
		when(req.getRequestURI()).thenReturn(uri);
		
		cadvisor1client
			.when(request("/metrics"))
			.respond(response().withBody(loadTestResponseBody("/cadvisor-response.proto.metrics")));
		
		cadvisor2client
			.when(request("/metrics"))
			.respond(response().withBody(loadTestResponseBody("/cadvisor2-response.proto.metrics")));

		//exercise
		createTestSubclassOfMetricsServlet().doGet(req, resp);
		//verify
		cadvisor1client.verify(request("/metrics"), VerificationTimes.exactly(1));
		cadvisor2client.verify(request("/metrics"), VerificationTimes.exactly(1));
		verify(resp).setContentType(MetricsServlet.PROMETHEUS_PROTOBUF_CONTENT_TYPE);
		MetricsResponse response = new MetricsResponse(new ByteArrayInputStream(baos.toByteArray()), null);
		return response;
	}

	private MetricsServlet createTestSubclassOfMetricsServlet() {
		return new MetricsServlet() {
			private static final long serialVersionUID = 1L;

			protected PrometheusMetricsEndpointLookup createMetricsEndpointLookup() {
				return new PrometheusMetricsEndpointLookup() {
					
					@Override
					public List<String> getAllAddresses(String name) {
						ArrayList<String> ads = new ArrayList<>();
						ads.add("http://localhost:" + cadvisor1.getPort() + "/metrics");
						ads.add("http://localhost:" + cadvisor2.getPort() + "/metrics");
						return ads;
					}
				};
			}
		};
	}

	private byte[] loadTestResponseBody(String file) throws IOException {
		try(InputStream is = getClass().getResourceAsStream(file)) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}

			buffer.flush();

			return buffer.toByteArray();
		}
	}
}
