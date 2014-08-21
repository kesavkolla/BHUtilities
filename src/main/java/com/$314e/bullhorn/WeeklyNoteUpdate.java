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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WeeklyNoteUpdate extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(WeeklyNoteUpdate.class);
	private final Configuration appConfig;

	public WeeklyNoteUpdate() throws Exception {
		super();
		this.appConfig = getConfig();
		doUpdate();
	}

	private void doUpdate() throws Exception {
		LOGGER.entry();

		List<String> candidateids = null;
		final Path candidatefile = FileSystems.getDefault().getPath("candidates.list");
		if (Files.exists(candidatefile)) {
			candidateids = Files.readAllLines(candidatefile);
		} else {
			candidateids = new ArrayList<>();
		}

		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		final String lastWeek = LocalDate.now().minusDays(9).format(DateTimeFormatter.ISO_LOCAL_DATE);
		LOGGER.debug("finding notes from: {}", lastWeek);

		final StringBuilder query = new StringBuilder();
		query.append("select personReferenceID, max(dateAdded) maxdate from dbo.Note")
				.append(" inner join dbo.NoteEntity on NoteEntity.noteID = Note.noteID and NoteEntity.entityName = 'Person'")
				.append(" where Note.isDeleted = 0 and").append(" dateAdded >= '").append(lastWeek).append("' and")
				.append(" action in ('Outbound Call', 'Inbound Call', 'Prescreened', 'Interview')")
				.append(" group by Note.personReferenceID").append(" order by Note.personReferenceID");
		Throwable exception = null;
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();
				final ResultSet rs = stmt.executeQuery(query.toString());
				final BufferedWriter writer = Files.newBufferedWriter(candidatefile, Charset.forName("US-ASCII"),
						StandardOpenOption.CREATE, StandardOpenOption.APPEND);) {
			while (rs.next()) {
				final int candidateId = rs.getInt("personReferenceID");
				if (candidateids.contains("" + candidateId)) {
					continue;
				}
				final Date maxdate = rs.getDate("maxdate");
				LOGGER.debug("Updating customtext2 of candidate:{} with date:{}", candidateId, maxdate);
				final ObjectNode data = JsonNodeFactory.instance.objectNode();

				data.put("customDate2", maxdate.getTime());

				try {
					final ObjectNode result = getEntityApi().update(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE,
							getRestToken(), candidateId, data);
					LOGGER.debug(result);
					writer.write("" + candidateId);
					writer.newLine();
					writer.flush();
				} catch (final Throwable t) {
					exception = t;
				}
			}
			if (exception != null) {
				throw new RuntimeException(exception);
			}
			Files.delete(candidatefile);
		}

		LOGGER.exit();
	}

	public static void main(final String... args) {
		try {
			new WeeklyNoteUpdate();
		} catch (final Throwable th) {
			LOGGER.error("Error in WeeklyNoteUpdate", th);
			System.exit(10);
		}
	}

}
