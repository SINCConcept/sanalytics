package at.ac.tuwien.dsg.sanalytics.sensor;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

import com.google.common.collect.Maps;

public class DataReader {
	
	public static interface Waiter {
		public void waitFor(long ms);
	}

	private Scanner sc;
	private String col1Value;
	private boolean datasetFound = false;
	private Long initialTime;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	private long startTime;
	private Waiter waiter;

	/**
	 * 
	 * @param col1Value
	 * @param is
	 *            we expect this stream
	 *            <ul>
	 *            <li>to be in CSV-format</li>
	 *            <li>to be sorted by the first column, first</li>
	 *            <li>then by the third which is a UTC-timestamp</li>
	 *            </ul>
	 */
	public DataReader(String col1Value, InputStream is, Waiter w) {
		this.col1Value = col1Value;
		this.waiter = w;
		this.sc = new Scanner(is);
	}

	public DataReader(String col1Value, InputStream is) {
		this(col1Value, is, new Waiter() {
			
			@Override
			public void waitFor(long ms) {
				try {
					Thread.sleep(ms);
				} catch (InterruptedException e) {
					throw new RuntimeException("unexpected interrupt exception");
				}
			}
		});
	}

	public Map.Entry<String, String> nextValue(long currentTimeMillis) {
		if (!sc.hasNextLine())
			return null;

		String l = sc.nextLine();
		if (!datasetFound) {
			while (!l.startsWith(col1Value)) {
				l = sc.nextLine();
				if (l == null)
					return null;
			}
			datasetFound = true;
		} else {
			if (!l.startsWith(col1Value))
				return null;
		}
		//l surely starts with col1Value at this point
		String[] cols = l.split(",");
		if(initialTime == null){
			initialTime = parseDate(cols).getTime();
			startTime = currentTimeMillis;
			return getValue(cols);
		}
		
		long eventTime = parseDate(cols).getTime();
		long eventMsAfterStart = eventTime - initialTime;
		long nowMsAfterStart = currentTimeMillis - startTime;
		long waitTime = eventMsAfterStart - nowMsAfterStart;
		
		if(waitTime > 0) {
			waiter.waitFor(waitTime);
		}
		return getValue(cols);
	}

	private Map.Entry<String, String> getValue(String[] cols) {
		return Maps.immutableEntry(cols[1], cols[3]);
	}

	private Date parseDate(String[] cols) {
		try {
			Date d = sdf.parse(cols[2]);
			return d;
		} catch (ParseException e) {
			throw new RuntimeException("unexpected wrong date format " + cols[2]);
		}
	}
}
