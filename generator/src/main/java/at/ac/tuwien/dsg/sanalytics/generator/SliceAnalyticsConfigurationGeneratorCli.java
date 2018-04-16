package at.ac.tuwien.dsg.sanalytics.generator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SliceAnalyticsConfigurationGeneratorCli {

	public static void main(String[] args) throws IOException {
		final File outdir = new File("target/output/slice");
		outdir.mkdirs();
		
		try (FileReader fr = new FileReader(new File("src/test/resources/my-slice-description-format.yml"));) {
			String sliceId = "slice1";
			new Generator(sliceId, fr, new WriterProvider() {
				
				@Override
				public Writer getWriter(String subsliceName, String file) throws IOException {
					String prefix = subsliceName != null ? subsliceName : sliceId;
					File pcOutFile = outdir.toPath().resolve(prefix + "." + file).toFile();
					return new FileWriter(pcOutFile);
				}
			}).generate();
		}
	}
}
