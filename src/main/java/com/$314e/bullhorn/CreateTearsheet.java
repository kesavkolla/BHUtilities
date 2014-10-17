package com.$314e.bullhorn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateTearsheet extends BaseUtil {
	private static final Logger LOGGER = LogManager.getLogger(CreateTearsheet.class);

	public CreateTearsheet() throws Exception {
		super();
		doExecute();
	}

	private void doExecute() throws Exception {
		LOGGER.entry();

		// Check the required properties present
		if (!appConfig.containsKey("sales.manager")) {
			throw new RuntimeException(
					"Can't find sales.manager property from properties file.  Create createtearsheet.properties and provide sales.manager property");
		}
		final Map<String, String> userNames = new HashMap<>();

		// User details query
		final StringBuilder userQuery = new StringBuilder();
		userQuery.append("SELECT userID, email,name FROM dbo.CorporateUser WHERE").append(" email IN (");
		for (final String mgr : appConfig.getStringArray("sales.manager")) {
			userNames.put(mgr.toLowerCase(), null);
			for (final String email : mgr.split(";")) {
				userQuery.append("'").append(email).append("',");
			}
		}
		userQuery.setLength(userQuery.length() - 1);
		userQuery.append(")");
		LOGGER.debug(userQuery);

		// Contact query
		final StringBuilder contactQuery = new StringBuilder();
		contactQuery
				.append("SELECT ClientContact.* FROM dbo.ClientCorporation")
				.append(" INNER JOIN dbo.ClientContact ON ClientContact.clientCorporationID = ClientCorporation.clientCorporationID")
				.append(" WHERE ClientCorporation.customText5 = 'Super High'")
				.append(" AND ClientContact.status IN ('Key Contact', 'Key Manager')").append(" AND ${userName} ");

		// Setup connection to SQL Server
		try (final Connection con = DriverManager.getConnection(appConfig.getString("BH_DATAMART_URL"),
				appConfig.getString("BH_DATAMART_USER"), appConfig.getString("BH_DATAMART_PASSWORD"));
				final Statement stmt = con.createStatement();) {

			// Get all the corporate user names for the given sales.manager
			// email addresses
			try (final ResultSet rs = stmt.executeQuery(userQuery.toString())) {
				while (rs.next()) {
					final String email = rs.getString("email").toLowerCase();
					final String name = rs.getString("name");
					if (userNames.containsKey(email)) {
						userNames.replace(email, name);
					} else {
						// Handle multiple email address scenario
						userNames.keySet().forEach(key -> {
							if (key.contains(email)) {
								final String curVal = userNames.get(key);
								if (StringUtils.isEmpty(curVal)) {
									userNames.replace(key, name);
								} else {
									userNames.replace(key, curVal + ";" + name);
								}
							}
						});
					}
				}
			}

			// For each sales manager get their important contacts
			for (final String userName : userNames.values()) {
				// Prepare the user name query part
				final Map<String, String> lookup = new HashMap<>();
				if (userName.indexOf(';') == -1) {
					lookup.put("userName", "ClientCorporation.customText9 LIKE '" + userName + "%'");
				} else {
					final String[] parts = userName.split(";");
					final StringBuilder buf = new StringBuilder();
					buf.append("(ClientCorporation.customText9 LIKE '%").append(parts[0]).append("%'");
					for (int i = 1; i < parts.length; i++) {
						buf.append(" OR ClientCorporation.customText9 LIKE '%").append(parts[i]).append("%'");
					}
					buf.append(")");
					lookup.put("userName", buf.toString());
				}

				// Get the query by replacing the place holders
				final String query = new StrSubstitutor(lookup).replace(contactQuery.toString());
				LOGGER.debug(query);

				try (final ResultSet rs = stmt.executeQuery(query)) {
					while (rs.next()) {

					}
				}
			}
		}
		LOGGER.exit();
	}

	public static void main(final String... args) {
		try {
			new CreateTearsheet();
		} catch (final Throwable th) {
			LOGGER.error("Error in Create TearSheet", th);
			System.exit(10);
		}

	}
}
