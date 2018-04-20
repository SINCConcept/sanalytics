package at.ac.tuwien.dsg.sanalytics.bridge.sql;

import java.util.Optional;

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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import at.ac.tuwien.dsg.sanalytics.events.Datapoint;
import io.prometheus.client.Counter;

@Component("outboundInterceptor")
@Profile("esper-cep-datapoint-sqlinterceptor")
@Import({ JdbcTemplateAutoConfiguration.class, DataSourceAutoConfiguration.class,
		FlywayAutoConfiguration.class })
public class SqlDatapointCEPResultOutboundInterceptor extends ChannelInterceptorAdapter {

	private static final String DEFAULT_MERGE_SQL = "MERGE INTO Datapoint(station, datapoint, val) values(?, ?, ?);";

	private final static Logger LOG = LoggerFactory
			.getLogger(SqlDatapointCEPResultOutboundInterceptor.class);

	@Autowired
	private DataSource dataSource;

	@Value("${esper-cep-datapoint-sqlinterceptor.query:}")
	private String query;

	private Counter datapointMergedToSqlDBCounter = Counter.build()
			.name("sql_outbound_intercept_total")
			.help("Total number of messages merged to a sql db").register();

	private Counter datapointMergeToSqlFailedDBCounter = Counter.build()
			.name("sql_outbound_intercept_error_total").labelNames("exception_class")
			.help("Total number of errors that occured when accesing dtabase").register();

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
		try {
			String sql = Optional.ofNullable(query).filter(StringUtils::hasText)
					.orElse(DEFAULT_MERGE_SQL);
			
			jdbcTemplate.execute(sql, (PreparedStatementCallback<Void>) ps -> {
				ps.setString(1, dp.getStation());
				ps.setString(2, dp.getDatapoint());
				ps.setDouble(3, dp.getValue());
				LOG.info("executing '" + sql + "' with params [{},{},{}]", dp.getStation(),
						dp.getDatapoint(), dp.getValue());
				ps.execute();
				return null;
			});
			datapointMergedToSqlDBCounter.inc();
		} catch (DataAccessException e) {
			LOG.error("merge to sql-db failed", e);
			datapointMergeToSqlFailedDBCounter.labels(e.getMostSpecificCause().getClass().getName())
					.inc();

		}
	}
}
