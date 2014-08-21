package com.$314e.bullhorn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

public class EmailDate extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(EmailDate.class);

	private static final String APP_NAME = "Gmail API Quickstart";
	private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".store/gmail");
	private static final String USER = "me";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final DateFormat queryFormat = new SimpleDateFormat("yyyy/MM/dd");

	public EmailDate() throws Exception {
		super();
		doUpdate();
	}

	private void doUpdate() throws Exception {
		List<String> candidateids = null;
		final Path candidatefile = FileSystems.getDefault().getPath("candidates.list");
		if (Files.exists(candidatefile)) {
			candidateids = Files.readAllLines(candidatefile);
		} else {
			candidateids = new ArrayList<>();
		}

		// Get all the candidates by search
		int start = 0;
		ObjectNode candidates = getEntityApi().search(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE, getRestToken(),
				"isDeleted:0 AND NOT status:archive", "email, email2, email3, customDate1, id", "+id", 500, start);

		LOGGER.debug(candidates);

		ObjectNode updates;

		ObjectNode toUpdate;

		// Authorization of Gmail API
		final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		final FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

		LOGGER.debug("Loading secret from: {}", this.getClass().getResource("/client_secret.json"));
		final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(this
				.getClass().getResourceAsStream("/client_secret.json")));

		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=plus "
					+ "into plus-cmdline-sample/src/main/resources/client_secrets.json");
			System.exit(1);
		}

		final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
				"9523858024-1s828b0rlffn957r80ch50vivsequ7cl.apps.googleusercontent.com", "2T56GjlkKVpKU5rKSJi0ZWSj",
				Collections.singleton(GmailScopes.GMAIL_MODIFY)).setDataStoreFactory(dataStoreFactory).build();

		final Credential auth = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

		LOGGER.debug("Auth expiriation: {}", auth.getExpiresInSeconds());

		final Credential credential = auth;

		final Gmail gmail = new Gmail.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APP_NAME)
				.build();

		// date to be updated

		Date parsedDate;

		final Date queryDate = new Date();
		queryDate.setMonth(queryDate.getMonth() - 1);
		final String queryDateString = queryFormat.format(queryDate);

		long time;

		int i = 1;

		int count = 0;

		final BufferedWriter writer = Files.newBufferedWriter(candidatefile, Charset.forName("US-ASCII"),
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);

		// Run through each candidate
		for (int index = 0, size = candidates.path("total").intValue(); index < size; index++) {
			LOGGER.debug("index: {} ", index);

			if (index % 500 == 0 && index != 0) {
				start += 500;
				count = 0;
				candidates = getEntityApi().search(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE, getRestToken(),
						"isDeleted:0 AND NOT status:archive", "email, email2, email3, customDate1, id", "+id", 500,
						start);
			}

			// check whether candidate is already processed
			final String candidateid = "";
			if (candidateids.contains(candidates.path("data").get(count).path("id").asText())) {
				count++;
				continue;
			}

			final String email = candidates.path("data").get(count).path("email").asText();
			final String email2 = candidates.path("data").get(count).path("email2").asText();
			final String email3 = candidates.path("data").get(count).path("email3").asText();

			ListMessagesResponse list;
			String query;

			// find out which emails the candidate has
			if (email != "" && email2 == "" && (email3 == "" || email3.equals("Not Applicable"))) {
				query = "in:inbox from:" + "(+" + email + ") after:" + queryDateString;
				list = gmail.users().messages().list("bullhorn@314ecorp.com")
						.setQ("in:inbox from:" + "(+" + email + ") after:" + queryDateString).execute();
			} else if (email != "" && email2 != "" && (email3 == "" || email3.equals("Not Applicable"))) {
				query = "in:inbox (from:" + "(+" + email + ") OR from:" + "(+" + email2 + ")) after:" + queryDateString;
				list = gmail
						.users()
						.messages()
						.list("bullhorn@314ecorp.com")
						.setQ("in:inbox (from:" + "(+" + email + ") OR from:" + "(+" + email2 + ")) after:"
								+ queryDateString).execute();
			} else if (email != "" && email2 == "" && email3 != "") {
				query = "in:inbox (from:" + "(+" + email + ") OR from:" + "(+" + email3 + ")) after:" + queryDateString;
				list = gmail
						.users()
						.messages()
						.list("bullhorn@314ecorp.com")
						.setQ("in:inbox (from:" + "(+" + email + ") OR from:" + "(+" + email3 + ")) after:"
								+ queryDateString).execute();
			} else if (email != "" && email2 != "" && email3 != "") {
				query = "in:inbox (from:" + "(+" + email + ") OR from:" + "(+" + email2 + ") OR from:" + "(+" + email3
						+ ")) after:" + queryDateString;
				list = gmail
						.users()
						.messages()
						.list("bullhorn@314ecorp.com")
						.setQ("in:inbox (from:" + "(+" + email + ") OR from:" + "(+" + email2 + ") OR from:" + "(+"
								+ email3 + ")) after:" + queryDateString).execute();
			} else if (email == "" && email2 == "" && (email3 == "" || email3.equals("Not Applicable"))) {
				LOGGER.info(candidates.path("data").get(count).path("id"));
				count++;
				continue;
			} else {
				query = "in:inbox (from:" + "(+" + email + ") OR from:" + "(+" + email2 + ") OR from:" + "(+" + email3
						+ ")) after:" + queryDateString;
				list = gmail
						.users()
						.messages()
						.list("bullhorn@314ecorp.com")
						.setQ("in:inbox (from:" + "(+" + email + ") OR from:" + "(+" + email2 + ") OR from:" + "(+"
								+ email3 + ")) after:" + queryDateString).execute();
			}

			// Copy the list contents into a for-loop-readable arraylist

			final List<Message> messages = new ArrayList<Message>();
			while (list.getMessages() != null) {
				messages.addAll(list.getMessages());
				if (list.getNextPageToken() != null) {
					String pageToken = list.getNextPageToken();
					list = gmail.users().messages().list("bullhorn@314ecorp.com").setQ(query).setPageToken(pageToken)
							.execute();
				} else {
					break;
				}
			}

			// Check to see whether candidate has any messages

			if (list != null) {

				LOGGER.debug(messages);

				// Go through the messages of the candidate

				for (final Message msgId : messages) {
					LOGGER.debug("candidate id: {} ", candidates.path("data").get(count).path("id"));
					LOGGER.debug("############################    " + i++ + "    #############################");

					final Message message = gmail.users().messages().get("bullhorn@314ecorp.com", msgId.getId())
							.setFormat("full").execute();

					final MessagePartHeader to = message
							.getPayload()
							.getHeaders()
							.stream()
							.filter(h -> (h.getName().equals("To") && h.getValue().toLowerCase()
									.contains("bullhorn@314ecorp.com"))
									|| (h.getName().equalsIgnoreCase("Cc") && h.getValue().toLowerCase()
											.contains("bullhorn@314ecorp.com"))).findFirst().orElse(null);
					final MessagePartHeader from = message.getPayload().getHeaders().stream()
							.filter(h -> h.getName().equals("From") && h.getValue().toLowerCase().contains(email))
							.findFirst().orElse(null);
					final MessagePartHeader from2 = message.getPayload().getHeaders().stream()
							.filter(h -> h.getName().equals("From") && h.getValue().toLowerCase().contains(email2))
							.findFirst().orElse(null);
					final MessagePartHeader from3 = message.getPayload().getHeaders().stream()
							.filter(h -> h.getName().equals("From") && h.getValue().toLowerCase().contains(email3))
							.findFirst().orElse(null);

					final MessagePartHeader date = message.getPayload().getHeaders().stream()
							.filter(h -> h.getName().equals("Date")).findFirst().orElse(null);

					// Convert the date to format the candidate's date
					DateFormat dateFormat;

					if (date.getValue().charAt(0) != 'S' && date.getValue().charAt(0) != 'M'
							&& date.getValue().charAt(0) != 'T' && date.getValue().charAt(0) != 'W'
							&& date.getValue().charAt(0) != 'F') {
						dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss ZZZZ");
					} else if (date.getValue().charAt(8) == '0' || date.getValue().charAt(8) == '1') {

						dateFormat = new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss");

					} else {
						dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ");
					}

					parsedDate = dateFormat.parse(date.getValue());
					LOGGER.debug("{}    {}", parsedDate.getTime(), parsedDate);

					time = parsedDate.getTime();
					// Adjust the date to match local timezone
					switch (date.getValue().charAt((int) date.getValue().length() - 3)) {
					case '0':
						time = (parsedDate.getTime() - 25200000);
						break;
					case '1':
						time = (parsedDate.getTime() - 21600000);
						break;
					case '2':
						time = (parsedDate.getTime() - 18000000);
						break;
					case '3':
						time = (parsedDate.getTime() - 14400000);
						break;
					case '4':
						time = (parsedDate.getTime() - 10800000);
						break;
					case '5':
						time = (parsedDate.getTime() - 7200000);
						break;
					case '6':
						time = (parsedDate.getTime() - 3600000);
						break;
					case '7':
						time = parsedDate.getTime();
						break;
					default:
						time = parsedDate.getTime();
						break;

					}
					// Update the candidate's most recent inbound email with
					// the
					// date, represented as epoch milliseconds
					toUpdate = getEntityApi().get(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE, getRestToken(),
							candidates.path("data").get(count).path("id").intValue(), "customDate1");

					updates = getEntityApi().update(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE, getRestToken(),
							candidates.path("data").get(count).path("id").intValue(),
							((ObjectNode) toUpdate.path("data")).put("customDate1", time));

					if (from != null) {
						LOGGER.debug("from: {}", from.toPrettyString());
					}

					if (from2 != null) {
						LOGGER.debug("from2: {}", from2.toPrettyString());
					}

					if (from3 != null) {
						LOGGER.debug("from3: {}", from3.toPrettyString());
					}

					if (to != null) {
						LOGGER.debug("to: {}", to.toPrettyString());
					}

					LOGGER.debug("date: {}", date.toPrettyString());
					break;
				}

			}
			// Write to file
			LOGGER.debug("count: {}", candidates.path("data").get(count).path("id"));
			writer.write(candidates.path("data").get(count).path("id").asText());
			writer.newLine();
			writer.flush();
			count++;

		}

		writer.close();
		// Delete the file candidate.list
		Files.deleteIfExists(candidatefile);

	}

	public static void main(final String... args) {

		try {
			new EmailDate();
		} catch (final Exception e) {
			LOGGER.error("Error in EmailDate", e);
			System.exit(10);
		}
	}

}
