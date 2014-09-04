package com.$314e.bullhorn;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

public class EmailDate extends BaseUtil {
	private static final Logger LOGGER = LogManager.getLogger(EmailDate.class);

	public static final DateTimeFormatter GMAIL_DATE_TIME;
	static {
		// manually code maps to ensure correct data always used
		// (locale data can be changed by application code)
		Map<Long, String> dow = new HashMap<>();
		dow.put(1L, "Mon");
		dow.put(2L, "Tue");
		dow.put(3L, "Wed");
		dow.put(4L, "Thu");
		dow.put(5L, "Fri");
		dow.put(6L, "Sat");
		dow.put(7L, "Sun");
		Map<Long, String> moy = new HashMap<>();
		moy.put(1L, "Jan");
		moy.put(2L, "Feb");
		moy.put(3L, "Mar");
		moy.put(4L, "Apr");
		moy.put(5L, "May");
		moy.put(6L, "Jun");
		moy.put(7L, "Jul");
		moy.put(8L, "Aug");
		moy.put(9L, "Sep");
		moy.put(10L, "Oct");
		moy.put(11L, "Nov");
		moy.put(12L, "Dec");
		GMAIL_DATE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().optionalStart()
				.appendText(DAY_OF_WEEK, dow).appendLiteral(", ").optionalEnd()
				.appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE).appendLiteral(' ')
				.appendText(MONTH_OF_YEAR, moy).appendLiteral(' ')
				.appendValue(YEAR, 4)
				// 2 digit year not handled
				.appendLiteral(' ').appendValue(HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2)
				.optionalStart().appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).optionalEnd().appendLiteral(' ')
				.appendOffsetId().toFormatter(Locale.getDefault(Locale.Category.FORMAT));
	}

	public EmailDate() throws Exception {
		super();
		setupGmail();

		doUpdate();
	}

	private void doUpdate() throws Exception {
		LOGGER.entry();
		final Path candidatefile = Paths.get("candidates.list");
		List<String> candidateids = null;
		if (Files.exists(candidatefile)) {
			candidateids = Files.readAllLines(candidatefile);
		} else {
			candidateids = new ArrayList<>();
		}
		final StringBuilder query = new StringBuilder();
		query.append("SELECT userID, email, email2, email3 FROM dbo.Candidate WHERE status <> 'Archive' AND isDeleted = 0");
		LOGGER.debug("Executing query: {}", query);
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();
				final ResultSet rs = stmt.executeQuery(query.toString());
				final BufferedWriter writer = Files.newBufferedWriter(candidatefile, Charset.forName("US-ASCII"),
						StandardOpenOption.CREATE, StandardOpenOption.APPEND);) {
			while (rs.next()) {
				final String userID = rs.getString("userID");
				// If this candidate is alreay being processed continue
				if (candidateids.contains(userID)) {
					continue;
				}
				final String email = rs.getString("email");
				final String email2 = rs.getString("email2");
				final String email3 = rs.getString("email3");
				// If the email is blank continue
				if (StringUtils.isBlank(email)) {
					continue;
				}

				final StringBuilder mailQuery = new StringBuilder();
				mailQuery.append("in:inbox newer_than:7d (").append("from:").append(email);
				if (StringUtils.isNotBlank(email2)) {
					mailQuery.append(" OR from:").append(email2);
				}
				if (StringUtils.isNotBlank(email3)) {
					mailQuery.append(" OR from:").append(email3);
				}
				mailQuery.append(")");
				final ListMessagesResponse response = gmail.users().messages().list("me").setQ(mailQuery.toString())
						.execute();
				if (response.getResultSizeEstimate() < 1) {
					continue;
				}
				final Message message = gmail.users().messages().get("me", response.getMessages().get(0).getId())
						.setFormat("full").execute();
				final MessagePartHeader date = message.getPayload().getHeaders().stream()
						.filter(h -> h.getName().equals("Date")).findFirst().orElse(null);
				final ObjectNode updateData = JsonNodeFactory.instance.objectNode();
				updateData.put("customDate1", convertGmailDateToBH(date.getValue()));
				LOGGER.debug("updating user {} with {}", userID, updateData.toString());
				LOGGER.debug(getEntityApi().update(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE, getRestToken(), userID,
						updateData));

				writer.write(userID);
				writer.newLine();
				writer.flush();
			}
		}
		Files.delete(candidatefile);
		LOGGER.exit();
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	private static long convertGmailDateToBH(final String date) {
		LOGGER.entry(date);
		int indx = date.indexOf('(');
		final StringBuilder strDate = new StringBuilder();
		if (indx > -1) {
			strDate.append(date.substring(0, indx - 1));
		} else {
			strDate.append(date);
		}
		strDate.insert(strDate.length() - 2, ":");
		LOGGER.debug(strDate);
		return LOGGER.exit(ZonedDateTime.parse(strDate.toString(), GMAIL_DATE_TIME).toEpochSecond() * 1000);
	}

	public static void main(String... args) {
		try {
			new EmailDate();
		} catch (final Exception e) {
			LOGGER.error("Error in EmailDate", e);
			System.exit(10);
		}
	}
}
