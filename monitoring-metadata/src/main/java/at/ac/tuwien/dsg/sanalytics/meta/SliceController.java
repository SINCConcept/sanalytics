package at.ac.tuwien.dsg.sanalytics.meta;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.dsg.sanalytics.generator.SliceConfigGenerator;
import at.ac.tuwien.dsg.sanalytics.generator.WriterProvider;

@RestController
public class SliceController {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class SliceNotFoundException extends RuntimeException {
		public SliceNotFoundException(String id) {
			// TODO Auto-generated constructor stub
		}

		private static final long serialVersionUID = 1L;
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class SubsliceNotFoundException extends RuntimeException {
		public SubsliceNotFoundException(String sliceId, String subsliceId) {
			// TODO Auto-generated constructor stub
		}

		private static final long serialVersionUID = 1L;
	}

	private final static Logger LOG = LoggerFactory.getLogger(SliceController.class);

	@Autowired
	private SliceRepository repo;

	@RequestMapping(path = "/slices/{id}/graphDefinition", method = RequestMethod.GET,
			produces = { "text/plain" })
	public ResponseEntity<String> getGraphDefinition(@PathVariable("id") String id) {
		Slice s = repo.findOne(id);
		if (s != null)
			return new ResponseEntity<String>(s.getGraphDefinition(), HttpStatus.OK);

		LOG.info("slice '" + id + "' not found");
		return new ResponseEntity<>("slice '" + id + "' not found", HttpStatus.NOT_FOUND);
	}

	@RequestMapping(path = "/slices/{id}/graphDefinition", method = RequestMethod.PUT,
			produces = "text/plain")
	@Transactional
	public ResponseEntity<String> putGraphDefinition(@PathVariable("id") String id,
			@RequestBody String body) {
		Slice s = findOrNew(id);

		s.setGraphDefinition(body);
		repo.save(s);
		return new ResponseEntity<>(body, HttpStatus.OK);
	}

	private Slice findOrNew(String id) {
		Slice s = repo.findOne(id);
		if (s == null)
			s = new Slice(id);
		return s;
	}

	private static class SliceStringWriter extends StringWriter {

		private Consumer<String> consumer;

		public SliceStringWriter(Consumer<String> consumer) {
			this.consumer = consumer;
		}

		@Override
		public void close() throws IOException {
			super.close();
			consumer.accept(toString());
		}
	}

	@RequestMapping(path = "/slices/{id}/", method = RequestMethod.PUT,
			consumes = { "text/plain", "application/yml" }, produces = "text/plain")
	@Transactional
	public ResponseEntity<String> putSlice(@PathVariable("id") String id, @RequestBody String body)
			throws IOException {
		LOG.info("putSlice: " + id);
		Slice s = findOrNew(id).clear();

		try (StringReader sr = new StringReader(body)) {
			WriterProvider writerProvider = new WriterProvider() {

				@Override
				public Writer getWriter(String subsliceName, String file) throws IOException {
					if (subsliceName == null) {
						// the only thing not scoped to a subslice is the
						// mermaid-file.
						return new SliceStringWriter(s::setGraphDefinition);
					}
					SubsliceMetadata subslice = s.getOrNewSubSlice(subsliceName);
					SubSliceConfigurationFile f = new SubSliceConfigurationFile();
					f.setType(file);
					subslice.addConfigurationFile(f);
					return new SliceStringWriter(f::setContent);
				}
			};
			new SliceConfigGenerator(id, sr, writerProvider).generate();
		}
		repo.save(s);
		return new ResponseEntity<>(body, HttpStatus.OK);
	}

	@RequestMapping(path = "/slices/{id}/subslices/{subslice}/conf/{configfile:.*}",
			method = RequestMethod.GET, produces = { "text/plain" })
	public ResponseEntity<String> getConfigFile(@PathVariable("id") String id,
			@PathVariable("subslice") String subsliceName,
			@PathVariable("configfile") String configFile) {

		SubsliceMetadata subslice = findSubSlice(id, subsliceName);

		Optional<SubSliceConfigurationFile> ssCf = subslice.getConfigurationFiles().stream()
				.filter(cf -> Objects.equals(configFile, cf.getType())).findFirst();

		if (!ssCf.isPresent()) {
			LOG.info("configFile '" + configFile + "' not found");
			return new ResponseEntity<>("configFile '" + configFile + "' not found",
					HttpStatus.NOT_FOUND);
		}

		String body = ssCf.get().getContent();
		return new ResponseEntity<>(body, HttpStatus.OK);
	}

	private SubsliceMetadata findSubSlice(String id, String subsliceName) {
		Slice s = repo.findOne(id);
		if (s == null) {
			LOG.info("slice '" + id + "' not found");
			throw new SliceNotFoundException(id);
		}
		Optional<SubsliceMetadata> oSubslice = s.getSubslices().stream()
				.filter(ss -> Objects.equals(ss.getName(), subsliceName)).findFirst();
		if (!oSubslice.isPresent()) {
			LOG.info("subslice '" + subsliceName + "' not found");
			throw new SubsliceNotFoundException(s.getId(), subsliceName);
		}
		SubsliceMetadata subslice = oSubslice.get();
		return subslice;
	}

	// @RequestMapping(path = "/slices/{id}/subslices/{subslice}/stack.yml",
	// method = RequestMethod.GET, produces = { "text/plain" })
	// public ResponseEntity<String> getStackConfig(@PathVariable("id") String
	// id,
	// @PathVariable("subslice") String subsliceName) {
	// SubsliceMetadata subslice = findSubSlice(id, subsliceName);
	//
	// String body = subslice.getConfigurationFiles().stream()
	// .filter(cf -> Objects.equals(cf.getType(),
	// "docker-compose.yml")).findFirst().get()
	// .getContent();
	// return new ResponseEntity<>(body, HttpStatus.OK);
	// }

	@RequestMapping(path = "/slices/{id}/subslices/{subslice}/setup.sh", method = RequestMethod.GET,
			produces = { "text/plain" })
	public ResponseEntity<String> getSetupScript(@PathVariable("id") String id,
			@PathVariable("subslice") String subsliceName, HttpServletRequest req) {

		SubsliceMetadata subslice = findSubSlice(id, subsliceName);
		String stackname = subslice.getSlice().getId() + "_" + subslice.getName();

		StringBuilder sb = new StringBuilder();
		sb.append("mkdir -p ./").append(stackname).append('\n');
		// we navigate into that dir so the docker-compose-file only needs
		// relative paths within the subslice directory.
		sb.append("cd ").append(stackname).append('\n');

		String stackYml = "./" + "docker-compose.yml";
		sb.append(Scripts.CURL_S_SL).append(ControllerUtils.getBaseUrl(req)).append("slices/")
				.append(id).append("/subslices/").append(subsliceName)
				.append("/conf/docker-compose.yml").append(" > ").append(stackYml).append('\n');

		// TODO create prometheus dir, load prometheus-conf

		sb.append("mkdir -p ").append("prometheus/conf/").append('\n');
		sb.append(Scripts.CURL_S_SL).append(ControllerUtils.getBaseUrl(req)).append("slices/")
				.append(id).append("/subslices/").append(subsliceName)
				.append("/conf/prometheus.yml").append(" > ")
				.append("./prometheus/conf/prometheus.yml").append('\n');

		sb.append("mkdir -p ").append("prometheus/data/").append('\n');

		// docker stack deploy
		sb.append("echo 'SANALYTICS: deploying stack ").append(stackname).append("'").append('\n');
		sb.append("docker stack deploy -c ").append(stackYml).append(" ").append(stackname)
				.append('\n');

		// move back out so we are in the same dir we started.
		sb.append("cd ..").append('\n');
		String body = sb.toString();
		return new ResponseEntity<>(body, HttpStatus.OK);
	}
}
