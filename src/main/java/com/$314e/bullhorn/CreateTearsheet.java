package com.$314e.bullhorn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreateTearsheet extends BaseUtil {
	private static final Logger LOGGER = LogManager.getLogger(CreateTearsheet.class);

	public CreateTearsheet() throws Exception {
		super();
		doExecute();
	}

	private void doExecute() throws Exception {
		LOGGER.entry();

		LOGGER.debug(getEntityApi().update(BHRestApi.Entity.ENTITY_TYPE.TEARSHEAT, getRestToken(), "2257",
				JsonNodeFactory.instance.objectNode().put("isDeleted", true)));
		LOGGER.debug(getEntityApi().update(BHRestApi.Entity.ENTITY_TYPE.TEARSHEAT, getRestToken(), "2256",
				JsonNodeFactory.instance.objectNode().put("isDeleted", true)));
		LOGGER.debug(getEntityApi().update(BHRestApi.Entity.ENTITY_TYPE.TEARSHEAT, getRestToken(), "2255",
				JsonNodeFactory.instance.objectNode().put("isDeleted", true)));
		LOGGER.debug(getEntityApi().update(BHRestApi.Entity.ENTITY_TYPE.TEARSHEAT, getRestToken(), "2254",
				JsonNodeFactory.instance.objectNode().put("isDeleted", true)));
		LOGGER.debug(getEntityApi().update(BHRestApi.Entity.ENTITY_TYPE.TEARSHEAT, getRestToken(), "2253",
				JsonNodeFactory.instance.objectNode().put("isDeleted", true)));

		if (LOGGER != null) {
			return;
		}

		// Check the required properties present
		if (!appConfig.containsKey("sales.managers")) {
			throw new RuntimeException(
					"Can't find sales.manager property from properties file.  Create createtearsheet.properties and provide sales.manager property");
		}
		final int nummgrs = appConfig.getInt("sales.managers");

		final List<ObjectNode> corpUsers = new ArrayList<>();

		// User details query
		final StringBuilder userQuery = new StringBuilder();
		userQuery.append("SELECT userID, email,name FROM dbo.CorporateUser WHERE").append(" email IN (");
		for (int i = 1; i <= nummgrs; i++) {
			final String key = "sales.manager." + i;
			final String mgremail = appConfig.getString(key);
			final ObjectNode corpUser = JsonNodeFactory.instance.objectNode();
			corpUser.put("email", mgremail.toLowerCase());
			corpUser.put("key", key);
			corpUsers.add(corpUser);
			for (final String email : mgremail.split(";")) {
				userQuery.append("'").append(email).append("',");
			}
		}
		userQuery.setLength(userQuery.length() - 1);
		userQuery.append(")");
		userQuery.append(" ORDER BY CASE email ");
		int ordcnt = 1;
		for (final ObjectNode corpUser : corpUsers) {
			if (!corpUser.has("email")) {
				continue;
			}
			for (final String email : corpUser.get("email").asText().split(";")) {
				userQuery.append(" WHEN '").append(email).append("' THEN ").append(ordcnt++);
			}
		}
		userQuery.append(" END");
		LOGGER.debug(userQuery);

		// Contact query
		final StringBuilder contactQuery = new StringBuilder();
		contactQuery
				.append("SELECT ClientContact.userID, ClientContact.name FROM dbo.ClientCorporation")
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

					final ObjectNode corpUser = corpUsers.stream()
							.filter(node -> node.get("email").asText().contains(email)).findFirst().get();
					if (corpUser == null) {
						continue;
					}
					if (corpUser.has("userID")) {
						corpUser.put("name", corpUser.get("name").asText() + ";" + rs.getString("name"));
					} else {
						corpUser.put("userID", rs.getInt("userID"));
						corpUser.put("name", rs.getString("name"));
					}
				}
			}
			LOGGER.debug(corpUsers);

			// For each sales manager get their important contacts
			for (final ObjectNode corpUser : corpUsers) {
				if (!corpUser.has("userID")) {
					continue;
				}
				// Prepare the user name query part
				final Map<String, String> lookup = new HashMap<>();
				final String userName = corpUser.get("name").asText();
				if (userName.indexOf(';') == -1) {
					lookup.put("userName", "ClientCorporation.customText9 LIKE '%" + userName + "%'");
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
				// Get the contacts that match the sales manager
				final ObjectNode contactData = JsonNodeFactory.instance.objectNode();
				final ArrayNode cids = contactData.putObject("clientContacts").putArray("add");
				try (final ResultSet rs = stmt.executeQuery(query)) {
					while (rs.next()) {
						cids.add(rs.getInt("userID"));
					}
				}
				if (cids.size() > 0) {
					// Got some contacts create a public tear sheat
					// {"name":"KESAV - TEST","isPrivate":"false","owner":{"id":5749}}
					final ObjectNode tearsheet = JsonNodeFactory.instance.objectNode();
					tearsheet.put("name", appConfig.getString(corpUser.get("key").asText() + ".tearsheet"));
					tearsheet.put("isPrivate", "false");
					tearsheet.with("owner").put("id", corpUser.get("userID").asInt());

					final ObjectNode tearsheetEntity = getEntityApi().put(BHRestApi.Entity.ENTITY_TYPE.TEARSHEAT,
							getRestToken(), tearsheet);
					// Prepare contact data
					contactData.putArray("ids").add(tearsheetEntity.get("changedEntityId").asInt());
					LOGGER.debug(contactData);
					// Do a mass update
					LOGGER.debug(getEntityApi().massUpdate(BHRestApi.Entity.ENTITY_TYPE.TEARSHEAT, getRestToken(),
							contactData));
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
