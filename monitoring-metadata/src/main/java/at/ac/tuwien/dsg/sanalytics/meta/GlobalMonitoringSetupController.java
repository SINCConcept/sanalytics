package at.ac.tuwien.dsg.sanalytics.meta;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/globalmon")
public class GlobalMonitoringSetupController {

	@RequestMapping(path = "setup.sh", produces = "text/plain")
	public ResponseEntity<String> getSetupScript() {
		StringBuilder sb = new StringBuilder();
		
		// TODO
		return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
	}
}
