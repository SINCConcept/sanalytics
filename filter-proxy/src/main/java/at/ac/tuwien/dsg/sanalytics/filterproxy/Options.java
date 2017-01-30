package at.ac.tuwien.dsg.sanalytics.filterproxy;

class Options {

	static final String TARGET_DNSNAME = System.getProperty("target.dnsname", "tasks.cadvisor");

	static final String FILTER_CONTAINER_LABEL_NAME = System.getProperty("sanalytics.slice.label",
			"container_label_sanalytics_slice");

}
