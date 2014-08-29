package com.$314e.bullhorn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

		LocalDate periodStart = LocalDate.now();
		try {
			periodStart = LocalDate
					.parse(appConfig.getString("periodStart"), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
		} catch (final Throwable th) {
		}
		LocalDate periodEnd = LocalDate.now();
		try {
			periodEnd = LocalDate.parse(appConfig.getString("periodEnd"), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
		} catch (final Throwable th) {
		}
		final String strPeriodStart = periodStart.atTime(LocalTime.MIDNIGHT).format(DateTimeFormatter.ISO_DATE_TIME)
				.replace('T', ' ');
		final String strPeriodEnd = periodEnd.atTime(23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)
				.replace('T', ' ');
		LOGGER.debug("Period Start: {}     Period End: {}", strPeriodStart, strPeriodEnd);

		final StringBuilder allactivityQuery = new StringBuilder(1024 * 2);
		// Internal Submission
		allactivityQuery.append("select * from ( ");
		allactivityQuery.append("select 'Internal Submission' as activity, count(*) as cnt from dbo.JobSubmission")
				.append(" where dateAdded between {ts '").append(strPeriodStart).append("'} and {ts '")
				.append(strPeriodEnd).append("'} and sendingUserID={userID}");
		// Client Submission
		allactivityQuery
				.append(" union all ")
				.append("select 'Client Submission', count(*) from dbo.Sendout ")
				.append(" inner join dbo.JobSubmission on Sendout.jobOrderID = JobSubmission.jobOrderID and Sendout.candidateID = JobSubmission.candidateID")
				.append(" where Sendout.dateAdded between {ts '").append(strPeriodStart).append("'} and {ts '")
				.append(strPeriodEnd).append("'}").append("  and JobSubmission.sendingUserID={userID}");
		// Interviews
		allactivityQuery
				.append(" union all ")
				.append("select 'Interview', count(*) from dbo.Appointment")
				.append(" inner join dbo.JobSubmission on Appointment.jobOrderID = JobSubmission.jobOrderID and Appointment.candidateReferenceID = JobSubmission.candidateID")
				.append(" where dbo.Appointment.dateAdded between {ts '").append(strPeriodStart).append("'} and {ts '")
				.append(strPeriodEnd).append("'}").append(" and JobSubmission.sendingUserID={userID}");

		// Notes
		allactivityQuery.append(" union all ").append("select Note.action+'_Note', count(*) from dbo.NoteEntity ")
				.append(" inner join dbo.Note on NoteEntity.noteID = Note.noteID")
				.append(" where entityName='JobOrder'").append(" and  Note.dateAdded  between {ts '")
				.append(strPeriodStart).append("'} and {ts '").append(strPeriodEnd).append("'}")
				.append(" and Note.commentingPersonID={userID}").append(" group by Note.action");
		allactivityQuery.append(" ) tmp where cnt > 0");

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

				final String query = allactivityQuery.toString().replace("{userID}", recruiter.path("id").asText());
				LOGGER.debug(query);
				final ObjectNode recruiterData = JsonNodeFactory.instance.objectNode();
				recruiterData.put("name", recruiter.path("name").asText());
				try (final ResultSet rs = stmt.executeQuery(query);) {
					while (rs.next()) {
						recruiterData.with("allactivity").put(rs.getString(1), rs.getInt(2));
					}
				}
				LOGGER.debug(recruiterData);
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
