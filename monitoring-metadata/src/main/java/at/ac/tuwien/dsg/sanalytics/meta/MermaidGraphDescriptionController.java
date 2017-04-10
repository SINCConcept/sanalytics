package at.ac.tuwien.dsg.sanalytics.meta;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MermaidGraphDescriptionController {
		
	private final static Logger LOG = LoggerFactory.getLogger(MermaidGraphDescriptionController.class);

	@Autowired
	private SliceRepository repo;
	
	@RequestMapping(path = "/slices/{id}/graphDefinition"
			, method = RequestMethod.GET
			, produces = {"text/plain"})
	public ResponseEntity<String> getGraphDefinition(
			@PathVariable("id") String id) {
		Slice s = repo.findOne(id);
		if(s != null)
			return new ResponseEntity<String>(s.getGraphDefinition(), HttpStatus.OK);

		LOG.info("slice '" + id + "' not found");
		return new ResponseEntity<>("slice '" + id + "' not found", HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(path = "/slices/{id}/graphDefinition"
			, method = RequestMethod.PUT
			, produces = "text/plain")
	@Transactional
	public ResponseEntity<String> putGraphDefinition(
			@PathVariable("id") String id,
			@RequestBody String body) {
		Slice s = repo.findOne(id);
		if(s == null)
			s = new Slice(id);
		
		s.setGraphDefinition(body);
		repo.save(s);
		return new ResponseEntity<>(body, HttpStatus.OK);
	}
}
