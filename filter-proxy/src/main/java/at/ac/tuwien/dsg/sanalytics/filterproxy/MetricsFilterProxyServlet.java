package at.ac.tuwien.dsg.sanalytics.filterproxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.params.HttpParams;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

public class MetricsFilterProxyServlet extends ProxyServlet {

	private static final boolean TEST_MODE = System.getProperty("sanalytics.testmode", "false").equals("true");
	private static final long serialVersionUID = 1L;
	private DNSJavaDNSLookup dnsJavaDNSLookup = createDNSLookup();
	private HashSet<String> metricsCommentsSet;

	@Override
	protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
//		System.out.println("copyRequestHeaders(..)");
//		Enumeration<String> enu = servletRequest.getHeaderNames();
//		while(true) {
//			if(!enu.hasMoreElements())
//				break;
//			String h = enu.nextElement();
//			System.out.println("Header: " + h + ": " + servletRequest);
//		}
		super.copyRequestHeaders(servletRequest, proxyRequest);
		proxyRequest.removeHeaders("Accept");	
	}	
	
	@Override
	protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		//charset-encoding
		super.copyResponseHeaders(proxyResponse, servletRequest, servletResponse);
	}
	
	@Override
	protected void initTarget() throws ServletException {
		//empty
	}
	
	@Override
	protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse)
			throws IOException {
		HttpEntity entity = proxyResponse.getEntity();
		if (entity != null) {
			OutputStream servletOutputStream = servletResponse.getOutputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entity.writeTo(baos);
			HashSet<String> metricsCommentsSet = threadLocalMetricsCommentsSet.get();
			try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
					MetricsStreamFilter s = new MetricsStreamFilter(bais, metricsCommentsSet)) {

				byte[] buffer = new byte[50 * 1024];
				int len;
				while ((len = s.read(buffer)) != -1) {
					servletOutputStream.write(buffer, 0, len);
				}
			}
//			entity.writeTo(servletOutputStream);
		}
	}
	
	private static ThreadLocal<HashSet<String>> threadLocalMetricsCommentsSet
		= new ThreadLocal<HashSet<String>>() {
		protected java.util.HashSet<String> initialValue() {
			return new HashSet<>();
		}
		
	};

	private DNSJavaDNSLookup createDNSLookup() {
		return new DNSJavaDNSLookup();
	}

	@Override
	protected HttpClient createHttpClient(HttpParams hcParams) {
		ContentEncodingHttpClient client = new ContentEncodingHttpClient(hcParams);
		return client;
	}
	
	private List<InetAddress> getCAdvisorAdresses() {
		if(TEST_MODE) {			
			ArrayList<InetAddress> ads = new ArrayList<>();
			try {
				ads.add(InetAddress.getByName("192.168.99.100"));
				ads.add(InetAddress.getByName("192.168.99.101"));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ads;
		}
		List<InetAddress> addresses = dnsJavaDNSLookup.getAllAddresses("cadvisor");
		return addresses;
	}

	@Override
	protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
			throws ServletException, IOException {
		System.out.println("version 15.12.2016 19:06");
		List<InetAddress> addresses = getCAdvisorAdresses();
//		servletResponse.setCharacterEncoding("UTF-8");
		try {
			threadLocalMetricsCommentsSet.get().clear();
			for(InetAddress ia : addresses) {			
				String hostAddress = ia.getHostAddress();
				String hostAddressFull = "http://" + hostAddress + ":8080";
				System.out.println("host-address-full: " + hostAddressFull);
				servletRequest.setAttribute(ATTR_TARGET_URI, hostAddressFull);///metrics");
				//i think we don't need separate hosts
				//hm on the other hand there's an illegalStateException
				try {
					servletRequest.setAttribute(ATTR_TARGET_HOST, getHost(hostAddressFull));
					super.service(servletRequest, servletResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			//TODO.
			threadLocalMetricsCommentsSet.get().clear();
			System.out.println("response-content-type: " + servletResponse.getHeader("Content-Type"));
		}
	}
	
	private ConcurrentHashMap<String, HttpClient> clients = new ConcurrentHashMap<String, HttpClient>();
//
	private Object getHost(String hostAddress) throws URISyntaxException {
		URI uri = new URI(hostAddress);
		return URIUtils.extractHost(uri);
	}
}
