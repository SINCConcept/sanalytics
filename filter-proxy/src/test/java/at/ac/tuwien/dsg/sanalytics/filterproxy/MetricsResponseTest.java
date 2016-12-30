package at.ac.tuwien.dsg.sanalytics.filterproxy;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import io.prometheus.client.Metrics.Counter;
import io.prometheus.client.Metrics.LabelPair;
import io.prometheus.client.Metrics.Metric;
import io.prometheus.client.Metrics.Metric.Builder;
import io.prometheus.client.Metrics.MetricFamily;

public class MetricsResponseTest {
	
	private interface ListCollector {
		public List<ByteArrayOutputStream> asList(ByteArrayOutputStream baos1, ByteArrayOutputStream baos2);
	}

	private static Counter newCounter() {
		return Counter.newBuilder().setValue(5.0).build();
	}

	private static LabelPair newLabel(String labelName, String labelValue) {
		return LabelPair.newBuilder().setName(labelName).setValue(labelValue).build();
	}

	private Builder newSliceMetric(String sliceName) {
		return Metric.newBuilder().addLabel(newLabel(MetricsResponse.CONTAINER_LABEL_SANALYTICS_SLICE, sliceName))
				.setCounter(newCounter());
	}

	private Metric noSliceMetric() {
		return Metric.newBuilder().addLabel(newLabel("filteredOut", "1")).setCounter(newCounter()).build();
	}

	private InputStream protoMetricsInput() {
		return getClass().getResourceAsStream("/cadvisor-response.proto.metrics");
	}

	@Test
	public void testMerge1_2() throws IOException {
		verifyMerge((baos1, baos2) -> Arrays.asList(baos1, baos2));
	}
	
	@Test
	public void testMerge2_1() throws IOException {
		verifyMerge((baos1, baos2) -> Arrays.asList(baos2, baos1));
	}
	
	@Test
	public void testSampleCAdvisorResponse() {
		MetricsResponse resp = new MetricsResponse(protoMetricsInput(), "slice0");
		assertEquals(38, resp.getMetricsFamilies().size());
	}
	
	public void verifyMerge(ListCollector listCollector) throws IOException {
		MetricFamily occursOnce = MetricFamily.newBuilder()
				.setName("occursOnce")
				.addMetric(noSliceMetric())
				.addMetric(newSliceMetric("slice0").addLabel(newLabel("source", "1")).build())
				.addMetric(newSliceMetric("slice2").addLabel(newLabel("source", "1")).build())
				.build();

		MetricFamily occursTwice1 = MetricFamily.newBuilder()
				.setName("occursTwice")
				.addMetric(noSliceMetric())
				.addMetric(newSliceMetric("slice0").addLabel(newLabel("source", "1")).build())
				.addMetric(newSliceMetric("slice2").addLabel(newLabel("source", "1")).build())
				.build();

		MetricFamily occursTwice2 = MetricFamily.newBuilder()
				.setName("occursTwice")
				.addMetric(noSliceMetric())
				.addMetric(newSliceMetric("slice0").addLabel(newLabel("source", "2")).build())
				.addMetric(newSliceMetric("slice2").addLabel(newLabel("source", "2")).build())
				.build();
		
		ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		
		occursTwice1.writeDelimitedTo(baos1);
		occursOnce.writeDelimitedTo(baos1);
		
		occursTwice2.writeDelimitedTo(baos2);
		
		MetricsResponse mergedResponse = listCollector.asList(baos1, baos2).stream()
			.map(baos -> new ByteArrayInputStream(baos.toByteArray()))
			.map(bais -> new MetricsResponse(bais, "slice0"))
			.reduce((a,b) -> a.mergeWith(b))
			.get();
		
		Map<String, MetricFamily> mergedMFs = mergedResponse.getMetricsFamilies();
		assertEquals("should have 2 metricsFamilies", 2, mergedMFs.size());
		assertEquals("expected 1 metric but was: " + mergedMFs.get("occursOnce").toString(), 
				1, mergedMFs.get("occursOnce").getMetricCount());
		assertEquals(2, mergedMFs.get("occursTwice").getMetricCount());
	}
}
