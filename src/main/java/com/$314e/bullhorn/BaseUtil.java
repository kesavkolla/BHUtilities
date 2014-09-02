package com.$314e.bullhorn;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.$314e.bhrestapi.BHRestUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

public class BaseUtil {
	private static final Logger LOGGER = LogManager.getLogger(BaseUtil.class);
	protected Configuration appConfig;

	private static final String APP_NAME = "314e Bullhorn Utilities";
	private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".store/gmail");

	private String restToken;
	private BHRestApi.Entity entityApi;

	protected Gmail gmail;

	protected Session mailSession;

	public BaseUtil() throws Exception {
		LOGGER.entry();
		// Load JDBC driver
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

		// Setup the commons configuration
		final DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder("META-INF/config.xml");
		appConfig = factory.getConfiguration();

		// Setup BHRest API
		final ObjectNode token = BHRestUtil.getRestToken((String) appConfig.getProperty("BH_CLIENT_ID"),
				(String) appConfig.getProperty("BH_CLIENT_SECRET"), (String) appConfig.getProperty("BH_USER"),
				(String) appConfig.getProperty("BH_PASSWORD"));
		this.restToken = token.get("BhRestToken").asText();
		this.entityApi = BHRestUtil.getEntityApi(token);

		LOGGER.exit();
	}

	/**
	 * Setup Gmail service
	 * 
	 * @throws Exception
	 */
	protected void setupGmail() throws Exception {
		LOGGER.entry();
		// Authorization of Gmail API
		final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		final FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

		LOGGER.debug("Loading secret from: {}", this.getClass().getResource("/client_secret.json"));
		final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),
				new InputStreamReader(this.getClass().getResourceAsStream("/client_secret.json")));

		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=plus "
					+ "into plus-cmdline-sample/src/main/resources/client_secrets.json");
			System.exit(1);
		}

		final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
				JacksonFactory.getDefaultInstance(),
				"9523858024-1s828b0rlffn957r80ch50vivsequ7cl.apps.googleusercontent.com", "2T56GjlkKVpKU5rKSJi0ZWSj",
				Collections.singleton(GmailScopes.GMAIL_MODIFY)).setDataStoreFactory(dataStoreFactory).build();

		final Credential auth = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

		LOGGER.debug("Auth expiriation: {}", auth.getExpiresInSeconds());

		final Credential credential = auth;

		gmail = new Gmail.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential).setApplicationName(
				APP_NAME).build();

		LOGGER.exit();
	}

	/**
	 * setup SMTP session
	 * 
	 * @throws Exception
	 */
	protected void setupSMTP() throws Exception {
		final Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", appConfig.getString("mail.smtp.host", "smtp.gmail.com"));
		props.put("mail.smtp.port", appConfig.getString("mail.smtp.port", "587"));
		mailSession = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(appConfig.getString("mail.smtp.user"), appConfig
						.getString("mail.smtp.password"));
			}
		});
	}

	protected Configuration getConfig() {
		return this.appConfig;
	}

	protected BHRestApi.Entity getEntityApi() {
		return this.entityApi;
	}

	protected String getRestToken() {
		return this.restToken;
	}

	public static <E> Iterable<E> iterable(final Iterator<E> iterator) {
		if (iterator == null) {
			throw new NullPointerException();
		}
		return new Iterable<E>() {
			public Iterator<E> iterator() {
				return iterator;
			}
		};
	}

}
