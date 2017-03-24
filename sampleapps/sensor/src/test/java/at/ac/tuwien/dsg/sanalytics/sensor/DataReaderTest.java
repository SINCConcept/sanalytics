package at.ac.tuwien.dsg.sanalytics.sensor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.common.collect.Maps;

import at.ac.tuwien.dsg.sanalytics.sensor.DataReader.Waiter;

public class DataReaderTest {

	private final List<Long> waitTimes = new ArrayList<>();

	private Waiter waiter = new Waiter() {
		@Override
		public void waitFor(long ms) {
			waitTimes.add(ms);
		}
	};
	
	@Test
	public void test() throws IOException {
		String s = "col1,col2,event_time,value,xxx\n"
		+ "60629000,142,2017-02-26 10:47:46 UTC,31,\n"
		+ "60629000,142,2017-02-26 10:48:06 UTC,30,\n"
		+ "60629000,142,2017-02-26 10:48:08 UTC,28,\n"
		+ "61114021,142,2017-02-26 18:03:19 UTC,0,\n"
		+ "61114021,142,2017-02-26 18:03:24 UTC,0,\n"
		+ "61114021,144,2017-02-26 18:03:25 UTC,1,\n"
		+ "61114021,142,2017-02-26 18:03:32 UTC,0,\n"
		+ "61114021,142,2017-02-26 18:03:34 UTC,0,\n"
		+ "61114021,146,2017-02-26 18:03:34 UTC,1,\n"
		+ "61114021,142,2017-02-26 18:03:34 UTC,1,";
		try(@SuppressWarnings("deprecation")
		InputStream is = new StringBufferInputStream(s);
				BufferedInputStream bis = new BufferedInputStream(is)) {
			DataReader dr = new DataReader("61114021", is, waiter);
			
			assertKeyValue("142", "0", dr.nextValue(0));
			assertKeyValue("142", "0", dr.nextValue(1000), 4000);
			assertKeyValue("144", "1", dr.nextValue(5000), 1000);
			assertKeyValue("142", "0", dr.nextValue(6000), 7000);
			assertKeyValue("142", "0", dr.nextValue(13000), 2000);
			assertKeyValue("146", "1", dr.nextValue(15000));
			assertKeyValue("142", "1", dr.nextValue(15000));
			assertNull(dr.nextValue(15000));
		}
	}

	private void assertKeyValue(String key, String value, Entry<String, String> v) {
		assertKeyValue(key, value, v, 0);
	}

	private void assertKeyValue(String key, 
			String value, 
			Entry<String, String> v, 
			long waitTime) {
		assertEquals(Maps.immutableEntry(key, value), v);
		assertEquals("expected different wait-time-size: " + waitTimes, waitTime > 0 ? 1 : 0, waitTimes.size());
		if(waitTime > 0) {
			Long wt = waitTimes.remove(0);
			assertEquals(waitTime, wt.longValue());
		}
	}
}
