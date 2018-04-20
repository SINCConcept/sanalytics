package at.ac.tuwien.dsg.sanalytics.bridge.sql;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.sanalytics.events.Command;

@Component("outboundInterceptor")
@Profile("command-inframe-sqlinterceptor")
@Import({ JdbcTemplateAutoConfiguration.class, DataSourceAutoConfiguration.class,
		FlywayAutoConfiguration.class })
public class SqlInFrameCommandOutboundInterceptor extends ChannelInterceptorAdapter {

	private static final String SQL = "select * from Datapoint "
			+ "where datapointId = ? and val between ? and ?;";

	private final static Logger LOG = LoggerFactory
			.getLogger(SqlInFrameCommandOutboundInterceptor.class);

	@Autowired
	private DataSource dataSource;

	@Value("${command-inframe-sqlinterceptor.upperLimit:100.0}")
	private double upperLimit;

	@Value("${command-inframe-sqlinterceptor.lowerLimit:0.0}")
	private double lowerLimit;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		final Command dp;
		if (message.getPayload() instanceof Command) {
			dp = (Command) message.getPayload();
		} else if (message.getPayload() instanceof String) {
			String sPayload = message.getPayload().toString();
			try {
				dp = Command.from(sPayload);
			} catch (IllegalArgumentException e) {
				LOG.warn("no command payload: " + sPayload);
				return message;
			}
		} else {
			LOG.warn("cannot handle message payload for: " + message);
			return message;
		}
		List<Map<String, Object>> list = jdbcTemplate.queryForList(SQL,
				(PreparedStatementCallback<ResultSet>) ps -> {
					LOG.info("executing '" + SQL + "' with params [{},{},{}]", dp.getDatapointId(),
							lowerLimit, upperLimit);
					return ps.executeQuery();
				});
		if (list.isEmpty()) {
			LOG.info("discarding command because value for datapoint not in frame [{}-{}]",
					lowerLimit, upperLimit);
			return null;
		}
		return message;
	}
}
