package at.ac.tuwien.dsg.sanalytics.filterproxy;

import java.net.InetAddress;
import java.util.List;

/**
 * can perform a DNS lookup 
 * 
 * @author cproinger
 *
 */
public interface DNSLookup {

	List<InetAddress> getAllAddresses(String name);

}
