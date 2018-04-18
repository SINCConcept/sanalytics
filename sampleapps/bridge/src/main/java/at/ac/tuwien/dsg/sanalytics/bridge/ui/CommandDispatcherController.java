package at.ac.tuwien.dsg.sanalytics.bridge.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.annotation.RequestScope;

import at.ac.tuwien.dsg.sanalytics.bridge.ui.ThymeleafConfig.CommandGateway;
import at.ac.tuwien.dsg.sanalytics.events.Command;
import io.prometheus.client.Counter;

@Profile("command-dispatcher-ui")
@Controller
public class CommandDispatcherController {

	@Autowired
	private CommandGateway gateway;
	
	private Counter dispatchedCommands = Counter.build()
			.name("commands_dispatched_total")
			.labelNames("datapointId", "actuatorId", "command")
			.help("The number of messages that have been bridged between input and output")
			.register();
	
	@GetMapping("/command-dispatcher")
	public String commandDispatcher(
//			@RequestParam(name = "datapoint", required = false, defaultValue = "") String datapoint,
			Model model) {
		Command command = new Command();
		model.addAttribute("command", command);
		return "command-dispatcher";
	}

	@PostMapping("/command-dispatcher")
	public String dispatchCommand(@ModelAttribute(name = "command") Command command,
			Model model) {
		gateway.sendCommand(command);
		dispatchedCommands
				.labels(command.getDatapointId(), command.getActuatorId(), command.getCommand())
				.inc();
		model.addAttribute("info", "successfully dispachted!");
		return "command-dispatcher";
	}
}