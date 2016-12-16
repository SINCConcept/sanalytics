package at.ac.tuwien.dsg.sanalytics.filterproxy;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DNSLookupDemo {

	public static void main(String[] args) throws TextParseException {
		long start = System.currentTimeMillis();
		String name = args.length == 0 ? "amazon.com" : args[0];
		{
			//the first request takes a long time
			Record[] records = new Lookup(name, Type.A).run();
			System.out.println("entries: " + records.length);
			for (int i = 0; i < records.length; i++) {
				Record mx = records[i];
				System.out.println("Host " + mx.toString());
			}
		}
		long afterFirst = System.currentTimeMillis();
		System.out.println("first request took " + (afterFirst - start) + " ms");
		{
			//the second one is fast
			Record[] records = new Lookup(name, Type.A).run();
			System.out.println("entries: " + records.length);
			for (int i = 0; i < records.length; i++) {
				ARecord mx = (ARecord) records[i];
				System.out.println("Host " + mx.getAddress().getHostAddress());
			}
		}
		System.out.println("second request took " + (System.currentTimeMillis() - afterFirst) + " ms");
	}
}
