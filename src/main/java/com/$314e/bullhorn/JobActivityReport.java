package com.$314e.bullhorn;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JobActivityReport extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(JobActivityReport.class);

	public JobActivityReport() throws Exception {
		super();
		doExecute();
	}

	private void doExecute() throws Exception {
		LOGGER.entry();
		final ObjectNode recruiters = getEntityApi().query(BHRestApi.Entity.ENTITY_TYPE.PERSON, getRestToken(),
				"isDeleted=false AND 93 MEMBER OF distributionLists AND firstName NOT IN ('Alok', 'Abhishek')",
				"id,name,email", "+id", 500, 0);

		for (final Iterator<JsonNode> iter = recruiters.path("data").elements(); iter.hasNext();) {
			final JsonNode recruiter = iter.next();
			LOGGER.debug(recruiter);

			final int recruiterId = recruiter.path("id").asInt();

		}
		LOGGER.exit();
	}

	public static void main(final String... args) {
		try {
			new JobActivityReport();
		} catch (final Throwable th) {
			LOGGER.error("Error in JobActivityReport", th);
			System.exit(10);
		}

	}

}
