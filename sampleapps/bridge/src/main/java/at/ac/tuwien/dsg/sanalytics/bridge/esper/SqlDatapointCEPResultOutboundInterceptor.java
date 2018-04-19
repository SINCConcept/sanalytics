package at.ac.tuwien.dsg.sanalytics.bridge.esper;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import at.ac.tuwien.dsg.sanalytics.events.Datapoint;

@Component("outboundInterceptor")
@Profile("esper-cep-datapoint-sqlinterceptor")
@Import({ JdbcTemplateAutoConfiguration.class, DataSourceAutoConfiguration.class,
		FlywayAutoConfiguration.class })
public class SqlDatapointCEPResultOutboundInterceptor extends ChannelInterceptorAdapter {

	private static final String MERGE_SQL = "MERGE INTO Datapoint(station, datapoint, val) values(?, ?, ?);";

	private final static Logger LOG = LoggerFactory.getLogger(SqlDatapointCEPResultOutboundInterceptor.class);
	
	@Autowired
	private DataSource dataSource;

	@Override
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		final Datapoint dp;
		if (message.getPayload() instanceof Datapoint) {
			dp = (Datapoint) message.getPayload();
		} else if (message.getPayload() instanceof String) {
			String sPayload = message.getPayload().toString();
			try {
				dp = Datapoint.from(sPayload);
			} catch (IllegalArgumentException e) {
				LOG.warn("no datapoint payload: " + sPayload);
				return;
			}
		} else {
			LOG.warn("cannot handle message payload for: " + message);
			return;
		}
		jdbcTemplate.execute(MERGE_SQL,
				(PreparedStatementCallback<Void>) ps -> {
					ps.setString(1, dp.getStation());
					ps.setString(2, dp.getDatapoint());
					ps.setDouble(3, dp.getValue());
					LOG.info("executing '" + MERGE_SQL + "' with params [{},{},{}]",
							dp.getStation(), dp.getDatapoint(), dp.getValue());
					ps.execute();
					return null;
				});
	}
}
