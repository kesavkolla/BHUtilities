package com.$314e.bullhorn;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActiveCandidates extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(ActiveCandidates.class);

	public ActiveCandidates() throws Exception {
		super();
		doUpdate();
	}

	private void doUpdate() throws Exception {

		final long cutoff = 90;
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
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
		final Path candidatefile = FileSystems.getDefault().getPath("candidates.list");
		final StringBuilder row = new StringBuilder();
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();
				final ResultSet rs = stmt.executeQuery(query.toString());
				final BufferedWriter writer = Files.newBufferedWriter(candidatefile, Charset.forName("UTF-8"),
						StandardOpenOption.CREATE, StandardOpenOption.APPEND);) {
			while (rs.next()) {
				row.setLength(0);
				row.append("\"").append(rs.getInt("userID")).append("\"");
				row.append(", \"").append(rs.getString("firstName")).append("\"");
				row.append(", \"").append(rs.getString("lastName")).append("\"");
				row.append(", \"").append(rs.getString("email")).append("\"");
				row.append("\", ").append(formatDate(rs.getDate("customDate1"))).append("\"");
				row.append("\", ").append(formatDate(rs.getDate("customDate2"))).append("\"");
				row.append("\", ").append(formatDate(rs.getDate("customDate3"))).append("\"");
				writer.write(row.toString());
				writer.newLine();
				writer.flush();
			}
		}

		// entityApi.update(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE,restToken,
		// candidates.path("data").get(count).path("id").asInt(),
		// ((ObjectNode)
		// candidates.path("data").get(count)).put("status", ""));
	}

	private String formatDate(final Date date) {
		if (date == null) {
			return "";
		}
		return date.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
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
