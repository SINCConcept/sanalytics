package at.ac.tuwien.dsg.sanalytics.generator;

import java.io.IOException;
import java.io.Writer;

public interface WriterProvider {
	public Writer getWriter(String subsliceName, String file) throws IOException;
}
