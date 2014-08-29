package com.$314e.bullhorn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JobActivityReport extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(JobActivityReport.class);
	private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

	public JobActivityReport() throws Exception {
		super();
		doExecute();
	}

	private void doExecute() throws Exception {
		LOGGER.entry();

		LocalDate periodStart = LocalDate.now();
		try {
			periodStart = LocalDate.parse(appConfig.getString("periodStart"), INPUT_DATE_FORMATTER);
		} catch (final Throwable th) {
		}
		LocalDate periodEnd = LocalDate.now();
		try {
			periodEnd = LocalDate.parse(appConfig.getString("periodEnd"), INPUT_DATE_FORMATTER);
		} catch (final Throwable th) {
		}
		final String strPeriodStart = periodStart.atTime(LocalTime.MIDNIGHT).format(DateTimeFormatter.ISO_DATE_TIME)
				.replace('T', ' ');
		final String strPeriodEnd = periodEnd.atTime(23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)
				.replace('T', ' ');
		final String allactivityQuery = buildAllActivityQuery(strPeriodStart, strPeriodEnd);
		final String jobActivityQuery = buildJobActivityQuery(strPeriodStart, strPeriodEnd);
		final String jobHistoryQuery = buildJobOrderHistoryQuery(strPeriodStart, strPeriodEnd);

		// Get all the recruiters from Bullhorn
		final ObjectNode recruiters = getEntityApi().query(BHRestApi.Entity.ENTITY_TYPE.PERSON, getRestToken(),
				"isDeleted=false AND 93 MEMBER OF distributionLists AND firstName NOT IN ('Alok', 'Abhishek')",
				"id,name,email", "+id", 500, 0);

		final ArrayNode emailData = JsonNodeFactory.instance.arrayNode();

		// Setup connection to SQL Server
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();) {

			// For each recruiter find jobs and it's details
			for (final Iterator<JsonNode> iter = recruiters.path("data").elements(); iter.hasNext();) {
				final JsonNode recruiter = iter.next();
				LOGGER.debug(recruiter);
				final ObjectNode recruiterData = JsonNodeFactory.instance.objectNode();
				recruiterData.put("name", recruiter.path("name").asText());
				// Job Activity query
				{
					final String query = jobActivityQuery.replace("{userID}", recruiter.path("id").asText()).replace(
							"{userName}", recruiter.path("name").asText());
					LOGGER.debug(query);
					try (final ResultSet rs = stmt.executeQuery(query);) {
						while (rs.next()) {

							recruiterData.with("jobactivity").with(rs.getString("jobOrderID"))
									.put("title", rs.getString("title")).put("status", rs.getString("status"))
									.put("isOpen", rs.getInt("isOpen")).with("activities")
									.put(rs.getString("activity"), rs.getInt("cnt"));

						}
					}
				}
				// JobOrder History query
				{
					final String query = jobHistoryQuery.replace("{userName}", recruiter.path("name").asText());
					LOGGER.debug(query);
					try (final ResultSet rs = stmt.executeQuery(query);) {
						while (rs.next()) {
							if (!recruiterData.has("jobactivity")) {
								break;
							}
							final ObjectNode jobActivityNode = recruiterData.with("jobactivity");
							if (jobActivityNode.has(rs.getString("jobOrderID"))) {
								jobActivityNode
										.with(rs.getString("jobOrderID"))
										.withArray("history")
										.addObject()
										.put("columnName", rs.getString("columnName"))
										.put("dateAdded",
												rs.getTimestamp("dateAdded").toLocalDateTime()
														.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
										.put("oldValue", rs.getString("oldValue"))
										.put("newValue", rs.getString("newValue"));
							}
						}
					}
				}
				// All activity query
				{

					final String query = allactivityQuery.replace("{userID}", recruiter.path("id").asText());
					LOGGER.debug(query);

					try (final ResultSet rs = stmt.executeQuery(query);) {
						while (rs.next()) {
							recruiterData.with("allactivity").put(rs.getString(1), rs.getInt(2));
						}
					}
				}
				LOGGER.debug(recruiterData);
				if (recruiterData.has("allactivity") || recruiterData.has("jobactivity")) {
					emailData.add(recruiterData);
				}
			}
		}

		LOGGER.debug(emailData);
		LOGGER.exit();
	}

	/**
	 * Build all activities query
	 * 
	 * @param strPeriodStart
	 * @param strPeriodEnd
	 * @return
	 */
	private String buildAllActivityQuery(final String strPeriodStart, final String strPeriodEnd) {
		LOGGER.entry(strPeriodStart, strPeriodEnd);
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

		return LOGGER.exit(allactivityQuery.toString());
	}

	/**
	 * 
	 * @param strPeriodStart
	 * @param strPeriodEnd
	 * @return
	 */
	private String buildJobActivityQuery(final String strPeriodStart, final String strPeriodEnd) {
		LOGGER.entry(strPeriodStart, strPeriodEnd);
		final StringBuilder query = new StringBuilder(1024 * 3);
		// Build CTE
		query.append("WITH Job_CTE(jobOrderID) AS(").append(" SELECT jobOrderID FROM dbo.EditHistoryJobOrder")
				.append(" WHERE columnName='correlatedCustomText2'").append(" AND dateAdded BETWEEN {ts '")
				.append(strPeriodStart).append("'} AND {ts '").append(strPeriodEnd).append("'}")
				.append(" AND (oldValue LIKE '%{userName}%' OR newValue LIKE '%{userName}%')").append(" UNION ")
				.append(" SELECT jobOrderID FROM dbo.JobOrder")
				.append(" WHERE correlatedCustomText2 like '%{userName}%' )");
		// Final Query
		query.append(" SELECT tmp.*, title, isOpen, status FROM (");
		// Internal Submission
		query.append(" ")
				.append(" SELECT Job_CTE.jobOrderID, 'Internal Submission' AS activity, COUNT(*) AS cnt FROM dbo.JobSubmission ")
				.append(" INNER JOIN Job_CTE ON Job_CTE.jobOrderID = JobSubmission.jobOrderID")
				.append(" WHERE dateAdded BETWEEN {ts '").append(strPeriodStart).append("'} AND {ts '")
				.append(strPeriodEnd).append("'} ").append(" AND sendingUserID={userID}")
				.append(" GROUP BY Job_CTE.jobOrderID");
		// Client Submission
		query.append(" UNION ALL ")
				.append(" SELECT Job_CTE.jobOrderID, 'Client Submission', COUNT(*) FROM dbo.Sendout")
				.append(" INNER JOIN Job_CTE ON Job_CTE.jobOrderID = Sendout.jobOrderID")
				.append(" INNER JOIN dbo.JobSubmission ON Sendout.jobOrderID = JobSubmission.jobOrderID AND Sendout.candidateID = JobSubmission.candidateID ")
				.append(" WHERE Sendout.dateAdded BETWEEN {ts '").append(strPeriodStart).append("'} AND {ts '")
				.append(strPeriodEnd).append("'}  ").append(" AND JobSubmission.sendingUserID={userID} ")
				.append(" GROUP BY Job_CTE.jobOrderID");
		// Interviews
		query.append(" UNION ALL ")
				.append(" SELECT Job_CTE.jobOrderID, 'Interview', COUNT(*) FROM dbo.Appointment")
				.append(" INNER JOIN Job_CTE ON Job_CTE.jobOrderID = Appointment.jobOrderID")
				.append(" INNER JOIN dbo.JobSubmission ON Appointment.jobOrderID = JobSubmission.jobOrderID AND Appointment.candidateReferenceID = JobSubmission.candidateID ")
				.append(" WHERE dbo.Appointment.dateAdded BETWEEN {ts '").append(strPeriodStart).append("'} AND {ts '")
				.append(strPeriodEnd).append("'} ").append(" AND JobSubmission.sendingUserID={userID}")
				.append(" GROUP BY Job_CTE.jobOrderID");
		// Notes
		query.append(" UNION ALL ")
				.append(" SELECT Job_CTE.jobOrderID, Note.action+'_Note', COUNT(*) FROM dbo.NoteEntity ")
				.append(" INNER JOIN Job_CTE ON Job_CTE.jobOrderID = NoteEntity.entityID")
				.append(" INNER JOIN dbo.Note ON NoteEntity.noteID = Note.noteID")
				.append(" WHERE entityName='JobOrder'").append(" AND  Note.dateAdded  BETWEEN {ts '")
				.append(strPeriodStart).append("'} AND {ts '").append(strPeriodEnd).append("'}")
				.append(" AND Note.commentingPersonID={userID}").append(" GROUP BY Job_CTE.jobOrderID, Note.action");
		// Finish final part
		query.append(" ) tmp").append(" INNER JOIN dbo.JobOrder ON JobOrder.jobOrderID = tmp.jobOrderID")
				.append(" ORDER BY tmp.jobOrderID");
		return LOGGER.exit(query.toString());
	}

	/**
	 * 
	 * @param strPeriodStart
	 * @param strPeriodEnd
	 * @return
	 */
	private String buildJobOrderHistoryQuery(final String strPeriodStart, final String strPeriodEnd) {
		LOGGER.entry(strPeriodStart, strPeriodEnd);
		final StringBuilder query = new StringBuilder(1024);
		query.append("SELECT * FROM dbo.EditHistoryJobOrder")
				.append(" WHERE columnName IN ('status', 'isOpen', 'correlatedCustomText2')")
				.append(" AND dateAdded BETWEEN {ts '").append(strPeriodStart).append("'} AND {ts '")
				.append(strPeriodEnd).append("'}")
				.append(" AND (oldValue LIKE '%{userName}%' OR newValue LIKE '%{userName}%')");
		return LOGGER.exit(query.toString());
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
