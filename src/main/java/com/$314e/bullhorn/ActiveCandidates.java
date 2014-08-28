package com.$314e.bullhorn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class updates the candidate status to Passive for all the candidates who
 * were added to system more than cutoff days and has no email or phone activity
 * for more than cutoff days
 * 
 * @author Kesav Kumar Kolla
 *
 */
public class ActiveCandidates extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(ActiveCandidates.class);

	public ActiveCandidates() throws Exception {
		super();
		doUpdate();
	}

	private void doUpdate() throws Exception {

		final long cutoff = 90;
		final StringBuilder query = new StringBuilder();
		// @formatter:off
		query.append("select userID, firstName, lastName, email, customDate1, customDate2, customDate3")
				.append(" from dbo.Candidate")
				.append(" where status = 'Actively Looking' and isDeleted = 0 ")
				.append(" and DATEDIFF(day, dateAdded, CURRENT_TIMESTAMP) > ").append(cutoff)
				.append(" and (customDate1 is null or DATEDIFF(day, customDate1, CURRENT_TIMESTAMP) > ").append(cutoff).append(")")
				.append(" and (customDate2 is null or DATEDIFF(day, customDate2, CURRENT_TIMESTAMP) > ").append(cutoff).append(")")
				.append(" and (customDate3 is null or DATEDIFF(day, customDate3, CURRENT_TIMESTAMP) > ").append(cutoff).append(")");
		// @formatter:on

		final ObjectNode data = JsonNodeFactory.instance.objectNode();
		final ArrayNode cids = data.putArray("ids");
		data.put("status", "Passive");
		int cnt = 1;
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();
				final ResultSet rs = stmt.executeQuery(query.toString());) {
			while (rs.next()) {
				if (cnt++ == 1000) {
					massUpdate(data);
					cnt = 1;
					cids.removeAll();
				}
				cids.add(rs.getInt("userID"));
			}
		}
		// update remaining ids
		if (cids.size() > 0) {
			massUpdate(data);
		}
	}

	/**
	 * Execute the massupdate
	 * 
	 * @param data
	 * @throws Exception
	 */
	private void massUpdate(final ObjectNode data) throws Exception {
		LOGGER.entry(data);

		final ObjectNode retVal = getEntityApi().massUpdate(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE, getRestToken(),
				data);
		LOGGER.info("updated the status");
		LOGGER.exit(retVal);
	}

	public static void main(final String... args) {
		try {
			new ActiveCandidates();
		} catch (final Throwable th) {
			LOGGER.error("Error in ActiveCandidates", th);
			System.exit(10);
		}
	}

}
