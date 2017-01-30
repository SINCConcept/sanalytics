package at.ac.tuwien.dsg.sanalytics.filterproxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HeaderElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeaderValueParser;

import io.prometheus.client.Metrics.Bucket;
import io.prometheus.client.Metrics.Histogram;
import io.prometheus.client.Metrics.LabelPair;
import io.prometheus.client.Metrics.Metric;
import io.prometheus.client.Metrics.MetricFamily;
import io.prometheus.client.Metrics.MetricType;
import io.prometheus.client.Metrics.Quantile;
import io.prometheus.client.Metrics.Summary;

public class MetricsSliceFilterServlet extends HttpServlet {

	private static final String PROMETHEUS_PROTOBUF_ACCEPT_HEADER = "application/vnd.google.protobuf";

	private final static class MetricsRequestFilter {
		private String sliceName;
		private String[] omitLabels;

		public MetricsRequestFilter(String sliceName) {
			this(sliceName, null);
		}

		/**
		 * @param omitLabels, can be used to filter out labels
		 * */
		public MetricsRequestFilter(String sliceName, String[] omitLabels) {
			this.sliceName = sliceName;
			this.omitLabels = omitLabels;
		}

		public MetricsResponse callMetricsEndpoint(String addr) {
			try(CloseableHttpClient client = CLIENT_BUILDER.build()) {
				HttpGet getMetricsRequest = new HttpGet(addr);
				getMetricsRequest.addHeader("Accept", PROMETHEUS_PROTOBUF_CONTENT_TYPE);
				System.out.println("calling " + addr);
				try(CloseableHttpResponse resp = client.execute(getMetricsRequest)) {
					return new MetricsResponse(resp.getEntity().getContent(), sliceName, omitLabels);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}
		
	}

	private static final HttpClientBuilder CLIENT_BUILDER = HttpClientBuilder.create()
		.disableCookieManagement()
		.disableAutomaticRetries();
	
	static final String PROMETHEUS_PROTOBUF_CONTENT_TYPE = "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited";

	private static final int REQUEST_URI_METRICS_PATH_LENGTH = "/metrics/".length();
	
	private static final long serialVersionUID = 1L;

	private PrometheusMetricsEndpointLookup dnsLookup = createMetricsEndpointLookup();

	protected PrometheusMetricsEndpointLookup createMetricsEndpointLookup() {
		return new PrometheusMetricsEndpointLookup() {
			
			@Override
			public List<String> getAllAddresses(String name) {
				return new DNSJavaDNSLookup().getAllAddresses(Options.TARGET_DNSNAME)
					.stream().map(a -> "http://" + a.getHostAddress() + ":8080/metrics")
					.collect(Collectors.toList());
//				ArrayList<String> ads = new ArrayList<>();
//				ads.add("http://192.168.99.100:8080/metrics");
//				ads.add("http://192.168.99.101:8080/metrics");
//				return ads;
			}
		};
	}
	
	private MetricsRequestFilter createSliceFilter(HttpServletRequest req) {
		//TODO make uri check better
		String sliceName = null;
		if(req.getRequestURI().startsWith("/metrics/") 
				&& req.getRequestURI().length() > REQUEST_URI_METRICS_PATH_LENGTH) {
			sliceName = req.getRequestURI().substring(REQUEST_URI_METRICS_PATH_LENGTH);
		}
		
		return new MetricsRequestFilter(sliceName, req.getParameterValues("omit_label"));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MetricsRequestFilter sliceFilter = createSliceFilter(req);
		
		
		MetricsResponse mr = dnsLookup.getAllAddresses("cadvisor")
			.parallelStream()
			.map(a -> sliceFilter.callMetricsEndpoint(a))
			.reduce(MetricsResponse.createEmpty(), (a,b) -> a.mergeWith(b));
		
		if(shouldSendProtobufResponse(req)) {
			resp.setContentType(PROMETHEUS_PROTOBUF_CONTENT_TYPE);
			mr.writeDelimitedTo(resp.getOutputStream());
		} else {
			resp.setContentType("text/plain; version=0.0.4");
			PrintWriter wr = resp.getWriter();
			printTextFormat(mr, wr);
		}
	}

	private boolean shouldSendProtobufResponse(HttpServletRequest req) {
		String accept = req.getHeader("Accept");
		HeaderElement selectedAcceptHeader = null;
		if(accept != null) {
			HeaderElement[] acceptElements = BasicHeaderValueParser.parseElements(accept, null);
			selectedAcceptHeader = Arrays.stream(acceptElements)
				.filter(he -> he.getName().startsWith("text/plain") 
						|| (he.getName().equals(PROMETHEUS_PROTOBUF_ACCEPT_HEADER)
								&& true)
						|| he.getName().startsWith("*/*"))
				.findFirst().get();
		}
		return selectedAcceptHeader == null
				|| selectedAcceptHeader.getName().equals(PROMETHEUS_PROTOBUF_ACCEPT_HEADER);
	}

	private void printMetricAndLabels(PrintWriter wr, String mfName, List<LabelPair> lables) {
		wr.print(mfName);
		if(lables.size() > 0) {
			wr.print("{");
			
			boolean first = true;
			for(LabelPair l : lables) {
				if(!first)
					wr.print(",");
				wr.print(l.getName());
				wr.print("=\"");
				wr.print(l.getValue());
				wr.print("\"");
				first = false;
			}
			wr.print("}");
		}
		wr.print(" ");
	}

	private void printTextFormat(MetricsResponse mr, PrintWriter wr) {
		for(MetricFamily mf : mr.getMetricsFamilies().values()) {
			String name = mf.getName();
			wr.print("# HELP ");
			wr.print(name);
			wr.print(" ");
			wr.println(mf.getHelp());
			
			wr.print("# TYPE ");
			wr.print(name);
			wr.print(" ");
			wr.println(mf.getType().toString().toLowerCase());
			
			for(Metric m : mf.getMetricList()) {
				
				List<LabelPair> metricLables = m.getLabelList();
				if(mf.getType().equals(MetricType.HISTOGRAM)) {
					Histogram h = m.getHistogram();
					for(Bucket b : h.getBucketList()) {
						LabelPair leLabel = LabelPair.newBuilder()
							.setName("le")
							.setValue(b.getUpperBound()+"")
							.build();
						ArrayList<LabelPair> bLabels = new ArrayList<>(metricLables);
						bLabels.add(leLabel);
						
						printMetricAndLabels(wr
								, name + "_bucket"
								, bLabels);
						wr.print(b.getCumulativeCount());
						if(m.hasTimestampMs()) {
							wr.print(" ");
							wr.print(m.getTimestampMs());
						}
					}
				} else if (mf.getType().equals(MetricType.SUMMARY)) {
					//a summary has 3 parts: n quantiles, a sum and a count
					Summary s = m.getSummary();
					//1. print quantiles
					for(Quantile q : s.getQuantileList()) {
						LabelPair quantileLabel = LabelPair.newBuilder()
							.setName("quantile")
							.setValue(q.getQuantile()+"")
							.build();
						ArrayList<LabelPair> qLabels = new ArrayList<>(metricLables);
						qLabels.add(quantileLabel);
						
						printMetricAndLabels(wr
								, name
								, qLabels);
						wr.print(q.getValue());
						if(m.hasTimestampMs()) {
							wr.print(" ");
							wr.print(m.getTimestampMs());
						}
						wr.println();
					}
					//2. print sum line
					printMetricAndLabels(wr
							, name + "_sum"
							, metricLables);
					wr.print(s.getSampleSum());
					if(m.hasTimestampMs()) {
						wr.print(" ");
						wr.print(m.getTimestampMs());
					}
					wr.println();
					//3. print count line
					printMetricAndLabels(wr
							, name + "_count"
							, metricLables);
					wr.print(s.getSampleCount());
					if(m.hasTimestampMs()) {
						wr.print(" ");
						wr.print(m.getTimestampMs());
					}
					wr.println();
				} else {
					printMetricAndLabels(wr, name, metricLables);
					if(m.hasCounter()) {
						wr.print(m.getCounter().getValue());
					} else if(m.hasGauge()) {
						wr.print(m.getGauge().getValue());
					} else if(m.hasUntyped()) {
						wr.print(m.getUntyped().getValue());
					} else {
						throw new RuntimeException("history and summary not implemented, type was: " + mf.getType());
					}
					if(m.hasTimestampMs()) {
						wr.print(" ");
						wr.print(m.getTimestampMs());
					}
					wr.println();
				}
			}
		}
	}
}
