package com.$314e.bullhorn;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.$314e.bhrestapi.BHRestUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BaseUtil {
	private static final Logger LOGGER = LogManager.getLogger(BaseUtil.class);
	private Configuration config;

	private String restToken;
	private BHRestApi.Entity entityApi;

	public BaseUtil() throws Exception {
		LOGGER.entry();
		final DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder("META-INF/config.xml");
		config = factory.getConfiguration();
		final ObjectNode token = BHRestUtil.getRestToken((String) config.getProperty("BH_CLIENT_ID"),
				(String) config.getProperty("BH_CLIENT_SECRET"), (String) config.getProperty("BH_USER"),
				(String) config.getProperty("BH_PASSWORD"));
		this.restToken = token.get("BhRestToken").asText();
		this.entityApi = BHRestUtil.getEntityApi(token);
		LOGGER.debug("Done with initializing BHRestAPI");
		LOGGER.exit();
	}

	protected Configuration getConfig() {
		return this.config;
	}

	protected BHRestApi.Entity getEntityApi() {
		return this.entityApi;
	}

	protected String getRestToken() {
		return this.restToken;
	}

}
