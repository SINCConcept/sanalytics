package at.ac.tuwien.dsg.sanalytics.filterproxy;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DNSJavaDNSLookup implements DNSLookup {

	@Override
	public List<InetAddress> getAllAddresses(String name) {
		ArrayList<InetAddress> adds = new ArrayList<>();

		// the first request takes a long time
		try {
			System.out.println("looking up dns A records for '" + name + "'");
			Record[] records = new Lookup(name, Type.A).run();
			System.out.println("entries: " + (records == null ? 0 : records.length));
			if(records == null)
				return new ArrayList<>();
			for (int i = 0; i < records.length; i++) {

				ARecord mx = (ARecord) records[i];
				System.out.println("Host " + mx.toString());
				adds.add(mx.getAddress());
			}
		} catch (TextParseException e) {
			e.printStackTrace();
		}
		return adds;
	}
}
