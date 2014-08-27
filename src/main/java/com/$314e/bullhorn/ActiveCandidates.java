package com.$314e.bullhorn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDate;

import com.$314e.bhrestapi.BHRestApi;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ActiveCandidates extends BaseUtil {

	private static final Logger LOGGER = LogManager.getLogger(ActiveCandidates.class);

	public ActiveCandidates() throws Exception {
		super();
		doUpdate();
	}

	private void doUpdate() throws Exception {

		final long cutoff = LocalDate.now().minusDays(90).toDate().getTime();

		int start = 0, count = 0, staying = 0, switching = 0;

		ObjectNode candidates = getEntityApi().search(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE, getRestToken(),
				"isDeleted:0 AND status:Currently Looking ",
				"id, dateAdded, customDate1, customDate2, customDate3, status", "+id", 500, start);

		for (int i = 0, len = candidates.path("total").asInt(); i < len; i++) {
			if (i % 500 == 0 && i != 0) {
				start += 500;
				count = 0;
				candidates = getEntityApi().search(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE, getRestToken(),
						" isDeleted:0 AND status:Currently Looking",
						"id, dateAdded, customDate1, customDate2, customDate3, status", "+id", 500, start);
			}

			if (candidates.path("data").get(count).path("dateAdded").asLong() > cutoff
					|| candidates.path("data").get(count).path("customDate1").asLong() > cutoff
					|| candidates.path("data").get(count).path("customDate2").asLong() > cutoff
					|| candidates.path("data").get(count).path("customDate3").asLong() > cutoff) {
				staying++;

			} else {
				switching++;
				// entityApi.update(BHRestApi.Entity.ENTITY_TYPE.CANDIDATE,restToken,
				// candidates.path("data").get(count).path("id").asInt(),
				// ((ObjectNode)
				// candidates.path("data").get(count)).put("status", ""));
			}
			count++;
		}
	}

	public static void main(final String... args) {
		try {
			new ActiveCandidates();
		} catch (final Throwable th) {
			LOGGER.error("Error in WeeklyNoteUpdate", th);
			System.exit(10);
		}
	}

}
