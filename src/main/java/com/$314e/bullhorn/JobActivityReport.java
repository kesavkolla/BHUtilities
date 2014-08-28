package com.$314e.bullhorn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JobActivityReport extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(JobActivityReport.class);

	public JobActivityReport() throws Exception {
		super();
		doExecute();
	}

	private void doExecute() throws Exception {
		LOGGER.entry();

		// Get all the recruiters from Bullhorn
		final ObjectNode recruiters = getEntityApi().query(BHRestApi.Entity.ENTITY_TYPE.PERSON, getRestToken(),
				"isDeleted=false AND 93 MEMBER OF distributionLists AND firstName NOT IN ('Alok', 'Abhishek')",
				"id,name,email", "+id", 500, 0);

		// Setup connection to SQL Server
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();) {
			int cnt = 1;

			// For each recruiter find jobs and it's details
			for (final Iterator<JsonNode> iter = recruiters.path("data").elements(); iter.hasNext();) {
				final JsonNode recruiter = iter.next();
				LOGGER.debug(recruiter);

				final int recruiterId = recruiter.path("id").asInt();

				final StringBuilder jobIdsQuery = new StringBuilder();
				jobIdsQuery
						.append("select JobOrder.jobOrderID, JobOrder.title, JobOrder.status, JobOrder.isOpen")
						.append(" from dbo.JobOrder")
						.append(" inner join dbo.JobOrderAssignedUsers on JobOrder.jobOrderID = JobOrderAssignedUsers.jobOrderID")
						.append(" and JobOrderAssignedUsers.isDeleted = 0").append(" where JobOrder.isDeleted = 0")
						.append(" and (JobOrder.ownerID = ").append(recruiterId)
						.append(" or JobOrderAssignedUsers.userID = ").append(recruiterId)
						.append(" or correlatedCustomText2 like '%").append(recruiter.path("name").asText().trim())
						.append("%' )");
				LOGGER.debug(jobIdsQuery);
				final ArrayNode jobOrders = JsonNodeFactory.instance.arrayNode();
				try (final ResultSet rs = stmt.executeQuery(jobIdsQuery.toString());) {
					while (rs.next()) {
						// @formatter:off
						jobOrders.addObject()
							.put("jobOrderID", rs.getInt("jobOrderID"))
							.put("title", rs.getString("title"))
							.put("status", rs.getString("status"))
							.put("isOpen", rs.getInt("isOpen"));
						// @formatter:on
					}
				}
				LOGGER.debug(jobOrders);
				LOGGER.debug(getJobOrderIds(jobOrders));

				if (cnt++ > 0) {
					break;
				}
			}
		}

		LOGGER.exit();
	}

	/**
	 * 
	 * @param jobOrders
	 * @return
	 */
	private String getJobOrderIds(final ArrayNode jobOrders) {
		LOGGER.entry();
		return LOGGER.exit(StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(jobOrders.elements(), Spliterator.IMMUTABLE), false)
				.map((item) -> item.path("jobOrderID").asText()).collect(Collectors.joining(",")));
	}

	public static void main(final String... args) {
		try {
			new JobActivityReport();
		} catch (final Throwable th) {
			LOGGER.error("Error in JobActivityReport", th);
			System.exit(10);
		}

	}

}
