package com.$314e.bullhorn;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MassUpdater {
	private static Logger logger = LogManager.getLogger(MassUpdater.class);
	private static Configuration config;

	public static void main(final String... args) throws Exception {
		logger.entry();
		final DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder("META-INF/config.xml");
		config = factory.getConfiguration();
		for (final Iterator<String> ite = config.getKeys(); ite.hasNext();) {
			final String key = ite.next();
			logger.debug("{}: {}", key, config.getProperty(key));
		}
		logger.exit();
	}
}
