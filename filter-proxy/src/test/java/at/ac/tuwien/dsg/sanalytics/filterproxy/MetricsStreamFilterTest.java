package at.ac.tuwien.dsg.sanalytics.filterproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import org.junit.Test;

public class MetricsStreamFilterTest {

	@Test
	public void test() throws IOException {
		InputStream is = getClass().getResourceAsStream("/cadvisor-metrics.txt");
		
		try(MetricsStreamFilter s = new MetricsStreamFilter(is, new HashSet<>())) {
			
			byte[] buffer = new byte[50*1024];
			int len;
			while ((len = s.read(buffer)) != -1) {
			    System.out.write(buffer, 0, len);
			}
		}
	}
}
