package at.ac.tuwien.dsg.sanalytics.filterproxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;

public class MetricsStreamFilter extends FilterInputStream {

    private static final Charset CHARSET = Charset.forName("UTF-8");
	private boolean end = false;
    //    final byte[] search, replacement;
	private BufferedReader input;
    private StringBuilder nextMetric = new StringBuilder();
    
    private ByteArrayInputStream singleMetricInputStream = null;
	private HashSet<String> metricsCommentsSet;

    public MetricsStreamFilter(InputStream is, HashSet<String> metricsCommentsSet) {
        super(is);
        this.input = new BufferedReader(new InputStreamReader(is, CHARSET));
        this.metricsCommentsSet = metricsCommentsSet;
//        this.search = search;
//        this.replacement = replacement;
    }


    @Override
    public void close() throws IOException {
    	super.close();
    	input.close();
    }
    
    private boolean doesFilterApply(String line) {
		return line.contains("container_label_sanalytics_slice=\"slice0\"");
	}
    
    @Override
    public int read() throws IOException {
    	if(end)
    		return -1;
    	
    	if(singleMetricInputStream == null)
    		if(!readAhead())
    			return -1;
    	
    	int read = singleMetricInputStream.read();
    	if(read == -1)
    		read = readAhead() ? singleMetricInputStream.read() : -1;
		return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
    	if(end)
    		return -1;
    	
    	if(singleMetricInputStream == null)
    		if(!readAhead())
    			return -1;
    	
    	int read = singleMetricInputStream.read(b, off, len);
    	if(read == -1)
    		read = readAhead() ? singleMetricInputStream.read(b, off, len) : -1;
		return read;
    }


	/**
     * reads the next metrics block. the singleMetricInputStream
     * contains the HELP and TYPE comment plus one or more metric lines. 
     * 
     * After this method runs, the nextMetric-sb contains the first
     * HELP-comment after the last metric, it will be added to singleMetricInput
     * on the next invocation of this method (if this metric has a line for which the filter applies). 
     * @return
     * @throws IOException
     */
    private boolean readAhead() throws IOException {
    	StringBuilder sb = new StringBuilder(nextMetric);
    	nextMetric = new StringBuilder();
    	String line = null;
    	boolean hashTagIsForNextMetric = false;
    	boolean metricPassedFilter = false;
    	
    	while((line = input.readLine()) != null) {
    		if(line.startsWith("#")) {
    			if(!metricPassedFilter && hashTagIsForNextMetric) {
    				sb = new StringBuilder();
    				hashTagIsForNextMetric = false;
    			}
    			
    			StringBuilder sbHash = metricPassedFilter ? nextMetric : sb;
    			if(metricsCommentsSet.contains(line))
    				line = "";
    			sbHash.append(line);
    			sbHash.append('\n');
    			metricsCommentsSet.add(line);
    			if(metricPassedFilter)
    				break;
    		} else {
    			//we have a metric here
    			hashTagIsForNextMetric = true;
    			if(!doesFilterApply(line)) {
    				continue;
    			}
    			metricPassedFilter = true;  			
    			sb.append(line);
    			sb.append('\n');
    		}
    	}
    	if(metricPassedFilter) {
	    	singleMetricInputStream = new ByteArrayInputStream(sb.toString().getBytes(CHARSET));
	    	return true;
    	}
    	singleMetricInputStream = null;
    	return false;
    }
}
