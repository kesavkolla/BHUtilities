package com.$314e.bullhorn;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SalesActivityReport extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(SalesActivityReport.class);
	private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	private static final ScriptEngineManager engineManager = new ScriptEngineManager();
	private final ScriptEngine scriptEngine;
	private final Invocable invocable;
	private final Object json;

	public SalesActivityReport() throws Exception {
		super();
		setupSMTP();

		/*
		 * Setup the script engine
		 */
		scriptEngine = engineManager.getEngineByName("nashorn");
		invocable = (Invocable) scriptEngine;

		try (final InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream(
				"/template/salesactivity.js"));
				final InputStreamReader reader1 = new InputStreamReader(this.getClass().getResourceAsStream(
						"/template/runtime.js"));
				final InputStreamReader reader2 = new InputStreamReader(this.getClass().getResourceAsStream(
						"/template/sugar.min.js"))) {
			scriptEngine.eval(reader2);
			scriptEngine.eval(reader1);
			scriptEngine.eval(reader);
		}
		json = scriptEngine.eval("JSON");
		doExecute();
	}

	private void doExecute() throws Exception {
		LOGGER.entry();
		LocalDate periodStart = LocalDate.now();
		try {
			periodStart = LocalDate.parse(appConfig.getString("periodStart"), INPUT_DATE_FORMATTER);
		} catch (final Throwable th) {
		}
		LocalDate periodEnd = periodStart;
		try {
			periodEnd = LocalDate.parse(appConfig.getString("periodEnd"), INPUT_DATE_FORMATTER);
		} catch (final Throwable th) {
		}
		final String strPeriodStart = periodStart.atTime(LocalTime.MIDNIGHT).format(DateTimeFormatter.ISO_DATE_TIME)
				.replace('T', ' ');
		final String strPeriodEnd = periodEnd.atTime(23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)
				.replace('T', ' ');
		final String allactivityQuery = buildAllActivityQuery(strPeriodStart, strPeriodEnd);

		// Get all the sales managers from Bullhorn. Distribution list Inside
		// Sales (1125)
		final ObjectNode salesmgrs = getEntityApi().query(BHRestApi.Entity.ENTITY_TYPE.PERSON, getRestToken(),
				"isDeleted=false AND 1125 MEMBER OF distributionLists ", "id,name,email", "+id", 500, 0);
		final String mgrids = StreamSupport.stream(salesmgrs.get("data").spliterator(), true)
				.map(mgr -> mgr.get("id").asText()).collect(Collectors.joining(","));
		LOGGER.debug("mgrids: {}", mgrids);

		final StringBuilder query = new StringBuilder();
		query.append("SELECT ")
				.append(" CorporateUser.userID, CorporateUser.name, CorporateUser.email")
				.append(" ,ClientCorporation.clientCorporationID, ClientCorporation.name")
				.append(" ,(SELECT COUNT(1) FROM ClientContact c WHERE c.clientCorporationID = ClientCorporation.clientCorporationID AND c.status = 'Key Manager') As numMgr")
				.append(" ,(SELECT COUNT(1) FROM ClientContact c WHERE c.clientCorporationID = ClientCorporation.clientCorporationID AND c.status = 'Key Contact') As numContact")
				.append(" ,ClientContact.name, Note.noteID, Note.action, Note.dateAdded")
				.append(" FROM dbo.Note")
				.append(" INNER JOIN dbo.CorporateUser ON CorporateUser.userID = Note.commentingPersonID")
				.append(" INNER JOIN dbo.ClientContact ON ClientContact.userID = Note.personReferenceID AND ClientContact.status IN ('Key Manager', 'Key Contact')")
				.append(" INNER JOIN dbo.ClientCorporation ON ClientCorporation.clientCorporationID = ClientContact.clientCorporationID")
				.append(" WHERE Note.dateAdded BETWEEN {ts '").append(strPeriodStart).append("'} AND {ts '")
				.append(strPeriodEnd).append("'}").append(" AND Note.commentingPersonID in (").append(mgrids)
				.append(")").append(" ORDER BY commentingPersonID, clientCorporationID")
				.append(" FOR XML AUTO, ELEMENTS, ROOT('EmailData')");

		LOGGER.debug(query);

		JSONObject mgrData = null;
		// Setup connection to SQL Server
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();) {

			mgrData = convertXML2JSON(stmt, query.toString());
			LOGGER.debug(mgrData);
			if (mgrData == null) {
				mgrData = new JSONObject("{EmailData:{CorporateUser:[]}}");
			} else {
				final JSONArray users = mgrData.getJSONObject("EmailData").getJSONArray("CorporateUser");
				for (int i = 0, len = users.length(); i < len; i++) {
					final JSONObject corpUser = users.getJSONObject(i);
					final String allquery = allactivityQuery.toString().replace("{userID}",
							"" + corpUser.getLong("userID"));
					final JSONObject allactivity = convertXML2JSON(stmt, allquery);
					if (allactivity == null) {
						corpUser.put("AllActivity", new JSONArray());
					} else {
						if (allactivity.getJSONObject("AllActivity").get("activity") instanceof JSONObject) {
							final JSONArray arr = new JSONArray();
							arr.put(allactivity.getJSONObject("AllActivity").getJSONObject("activity"));
							corpUser.put("AllActivity", arr);
						} else {
							corpUser.put("AllActivity",
									allactivity.getJSONObject("AllActivity").getJSONArray("activity"));
						}
					}
				}
			}
		}
		sendEmail(mgrData, periodStart, periodEnd);

		LOGGER.exit();
	}

	private JSONObject convertXML2JSON(final Statement stmt, final String query) throws Exception {
		LOGGER.entry(query);
		final StringBuilder sb = new StringBuilder();
		try (final ResultSet rs = stmt.executeQuery(query.toString());) {
			while (rs.next()) {
				sb.append(rs.getString(1));
			}
		}
		if (sb.length() < 1) {
			return LOGGER.exit(null);
		}
		final JSONObject jsonData = XML.toJSONObject(sb.toString().replaceAll("dbo\\.", ""));
		return LOGGER.exit(jsonData);
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
		allactivityQuery.append("select 'Internal Submission' as name, count(*) as cnt from dbo.JobSubmission")
				.append(" where dateAdded between {ts '").append(strPeriodStart).append("'} and {ts '")
				.append(strPeriodEnd).append("'} and sendingUserID={userID}");
		// Client Submission
		allactivityQuery.append(" union all ").append("select 'Client Submission', count(*) from dbo.Sendout ")
				.append(" where Sendout.dateAdded between {ts '").append(strPeriodStart).append("'} and {ts '")
				.append(strPeriodEnd).append("'}").append("  and userID={userID}");
		// Interviews
		allactivityQuery.append(" union all ").append("select 'Interview', count(*) from dbo.Appointment")
				.append(" where dbo.Appointment.dateAdded between {ts '").append(strPeriodStart).append("'} and {ts '")
				.append(strPeriodEnd).append("'}").append(" and ownerID={userID}");

		// Notes
		allactivityQuery.append(" union all ").append("select Note.action+'_Note', count(*) from dbo.Note")
				.append(" where Note.dateAdded  between {ts '").append(strPeriodStart).append("'} and {ts '")
				.append(strPeriodEnd).append("'}").append(" and Note.commentingPersonID={userID}")
				.append(" group by Note.action");
		allactivityQuery.append(" ) activity where cnt > 0 FOR XML AUTO, ROOT('AllActivity')");

		return LOGGER.exit(allactivityQuery.toString());
	}

	/**
	 * 
	 * @param emailData
	 * @throws Exception
	 */
	private void sendEmail(final JSONObject emailData, final LocalDate periodStart, final LocalDate periodEnd)
			throws Exception {
		LOGGER.entry(emailData);
		final String runDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss"));
		emailData.put("rundate", runDate);
		// Convert java json object to JavaScript json object
		final Object jsonData = invocable.invokeMethod(json, "parse", emailData.toString());
		// Execute compiled jade template
		final String emailText = (String) invocable.invokeFunction("template", jsonData);

		// Do style inlie for sending email
		final HttpPost httppost = new HttpPost("http://zurb.com/ink/skate-proxy.php");
		final List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("source", emailText));
		final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		httppost.setEntity(entity);
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode respData = objectMapper.readTree(EntityUtils.toString(httpclient.execute(httppost).getEntity()));
		String premailText = respData.path("html").asText();
		// Replace copy right and nbsp -- This is a fix for zrub inliner not
		// able to handle http entities properly
		premailText = premailText.replace("&amp; copy;", "&copy;").replace("&amp; nbsp;", "&nbsp;");

		// Send email to recipients
		final MimeMessage email = new MimeMessage(mailSession);
		if (Period.between(periodStart, periodEnd).getDays() != 0) {
			email.setSubject("Sales Activity Report for period: " + periodStart.format(INPUT_DATE_FORMATTER) + " - "
					+ periodEnd.format(INPUT_DATE_FORMATTER));
		} else {
			email.setSubject("Sales Activity Report for date: " + periodStart.format(INPUT_DATE_FORMATTER));
		}
		email.setFrom(appConfig.getString("mail.smtp.user"));
		email.setContent(premailText, "text/html; charset=utf-8");

		LOGGER.debug("sending email to {}", appConfig.getString("email.recipients"));
		for (final String receipent : appConfig.getString("email.recipients").split(";|,")) {
			email.addRecipients(RecipientType.TO, receipent);
		}
		Transport.send(email);

		LOGGER.exit();

	}

	public static void main(final String... args) {
		try {
			new SalesActivityReport();
		} catch (final Throwable th) {
			LOGGER.error("Error in SalesActivityReport", th);
			System.exit(10);
		}

	}

}
