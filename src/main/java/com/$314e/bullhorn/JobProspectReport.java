package com.$314e.bullhorn;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
import org.json.JSONObject;
import org.json.XML;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This report will go through all the notes for the job #16 and reports the
 * following information.
 * <ul>
 * <li>Company</li>
 * <li>Contact</li>
 * <li>Title</li>
 * <li>Note action</li>
 * </ul>
 * 
 * @author Kesav Kumar Kolla (kesav@314ecorp.com)
 *
 */
public class JobProspectReport extends BaseUtil {
	private static final Logger LOGGER = LogManager.getLogger(JobProspectReport.class);
	private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	private static final ScriptEngineManager engineManager = new ScriptEngineManager();
	private final ScriptEngine scriptEngine;
	private final Invocable invocable;
	private final Object json;

	public JobProspectReport() throws Exception {
		super();
		setupSMTP();

		/*
		 * Setup the script engine
		 */
		scriptEngine = engineManager.getEngineByName("nashorn");
		invocable = (Invocable) scriptEngine;

		try (final InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream(
				"/template/jobpropspect.js"));
				final InputStreamReader reader1 = new InputStreamReader(this.getClass().getResourceAsStream(
						"/template/runtime.js"))) {
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
		LocalDate periodEnd = LocalDate.now();
		try {
			periodEnd = LocalDate.parse(appConfig.getString("periodEnd"), INPUT_DATE_FORMATTER);
		} catch (final Throwable th) {
		}
		final String strPeriodStart = periodStart.atTime(LocalTime.MIDNIGHT).format(DateTimeFormatter.ISO_DATE_TIME)
				.replace('T', ' ');
		final String strPeriodEnd = periodEnd.atTime(23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)
				.replace('T', ' ');
		LOGGER.debug("start: {}    End: {}", strPeriodStart, strPeriodEnd);

		// Query the database for all the notes
		final StringBuilder query = new StringBuilder();
		query.append("SELECT CorporateUser.userID, CorporateUser.name, CorporateUser.email")
				.append(" ,ClientContact.userID, ClientContact.name, ClientContact.occupation, ClientContact.email")
				.append(" ,Note.noteID, Note.dateAdded, Note.action")
				.append(" ,ClientCorporation.clientCorporationID, ClientCorporation.name")
				.append(" FROM dbo.NoteEntity")
				.append(" INNER JOIN dbo.Note ON Note.noteID = NoteEntity.noteID")
				.append(" INNER JOIN dbo.ClientContact ON Note.personReferenceID = ClientContact.userID")
				.append(" INNER JOIN dbo.CorporateUser ON Note.commentingPersonID = CorporateUser.userID")
				.append(" INNER JOIN dbo.ClientCorporation ON ClientCorporation.clientCorporationID = ClientContact.clientCorporationID")
				.append(" WHERE NoteEntity.entityID = 16").append(" AND Note.dateAdded BETWEEN {ts '")
				.append(strPeriodStart).append("'} AND {ts '").append(strPeriodEnd).append("'}")
				.append(" ORDER BY Note.commentingPersonID, NoteEntity.noteID DESC")
				.append(" FOR XML AUTO, ELEMENTS, ROOT('EmailData')");
		LOGGER.debug(query);

		// Setup connection to SQL Server
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();
				final ResultSet rs = stmt.executeQuery(query.toString());) {

			final StringBuilder data = new StringBuilder();
			while (rs.next()) {
				data.append(rs.getString(1));
			}

			final JSONObject recruiterData = XML.toJSONObject(data.toString().replaceAll("dbo\\.", ""));
			sendEmail(recruiterData);
		}
		LOGGER.exit();
	}

	/**
	 * Prepares email from the jade template and sends it to the configured
	 * recipients.
	 * 
	 * @param inputNode
	 * @throws Exception
	 */
	private void sendEmail(final JSONObject inputNode) throws Exception {
		LOGGER.entry(inputNode);
		final String runDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss"));
		inputNode.put("rundate", runDate);
		// Convert java json object to JavaScript json object
		final Object jsonData = invocable.invokeMethod(json, "parse", inputNode.toString());
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
		email.setSubject("Job Prospect Follow-Up Daily Report for date: " + runDate);
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
			new JobProspectReport();
		} catch (final Throwable th) {
			LOGGER.error("Error in JobActivityReport", th);
			System.exit(10);
		}
	}

}
