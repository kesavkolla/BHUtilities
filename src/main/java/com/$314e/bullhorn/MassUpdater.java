package com.$314e.bullhorn;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.$314e.bhrestapi.BHRestApi;
import com.$314e.bhrestapi.BHRestUtil;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MassUpdater {
	private static Logger logger = LogManager.getLogger(MassUpdater.class);
	private static Configuration config;

	private static ObjectNode token;
	private static String restToken;
	private static BHRestApi.Entity entityApi;

	public static void main(final String... args) {
		try {

			List<String> candidateids = null;
			final Path candidateFile = FileSystems.getDefault().getPath("candidate.list");
			if (Files.exists(candidateFile)) {
				candidateids = Files.readAllLines(candidateFile);
			} else {
				candidateids = new ArrayList<>();
			}

			// Get the information from the property files
			logger.entry();
			final DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder("META-INF/config.xml");
			config = factory.getConfiguration();
			for (final Iterator<String> ite = config.getKeys(); ite.hasNext();) {
				final String key = ite.next();
				logger.debug("{}: {}", key, config.getProperty(key));
			}
			logger.exit();

			token = BHRestUtil.getRestToken((String) config.getProperty("BH_CLIENT_ID"),
					(String) config.getProperty("BH_CLIENT_SECRET"), (String) config.getProperty("BH_USER"),
					(String) config.getProperty("BH_PASSWORD"));
			restToken = token.get("BhRestToken").asText();
			entityApi = BHRestUtil.getEntityApi(token);

			// retrieve all the entity ids from the csv file and put into a list

			Scanner scanner = new Scanner(new File((String) config.getProperty("datafile")));

			final ObjectNode data = new ObjectNode(JsonNodeFactory.instance);

			final ArrayNode ids = data.putArray("ids");

			ArrayList<Object> list = new ArrayList<Object>();

			scanner.useDelimiter(",");
			scanner.nextLine();
			scanner.nextLine();
			try {
				while (scanner.hasNext()) {
					if (scanner.hasNextInt()) {
						list.add(scanner.nextInt());
					} else {
						list.add(scanner.next());
					}

					scanner.nextLine();
				}

			} catch (InputMismatchException e) {
				System.out.println(e);
			}
			scanner.close();

			// Convert the list of String ids to integers only if they were
			// strings to begin with and not ints

			for (int i = list.size() - 1; i >= 0; i--) {
				if (list.get(0) instanceof String) {
					list.set(
							i,
							Integer.parseInt((String) list.get(i).toString()
									.substring(1, list.get(i).toString().length() - 1)));
					ids.add((int) list.get(i));
				} else {
					ids.add((int) list.get(i));
				}
			}

			// Get the fieldname and value from the property file
			String fieldName = (String) config.getProperty("fieldName");
			Object fieldValue = config.getProperty("fieldValue");

			// put the value in the data to be updated
			data.put((String) config.getProperty("fieldName"), (String) config.getProperty("fieldValue"));
			System.out.println(data);

			String entity = (String) config.getProperty("entityType");
			System.out.println(entity);

			// Case to determine whether the entity field is supported by
			// massupdate. If it is not, then have to update each candidate
			// individually
			System.out.println(data);
			final BufferedWriter writer = Files.newBufferedWriter(candidateFile, Charset.forName("US-ASCII"),
					StandardOpenOption.CREATE);
			if (fieldName.equals("statuss") || fieldName.equals("businessSectors") || fieldName.equals("categories")
					|| fieldName.equals("isDeleted") || fieldName.equals("owner") || fieldName.equals("primarySkills")
					|| fieldName.equals("source") || fieldName.equals("specialties")) {
				entityApi.massUpdate(BHRestApi.Entity.ENTITY_TYPE.valueOf(entity), restToken, data);
			} else if ((int) fieldValue.toString().charAt(0) >= 48 && (int) fieldValue.toString().charAt(0) <= 57) {
				for (int index = 0; index < list.size(); index++) {
					ObjectNode candidate = entityApi.get(BHRestApi.Entity.ENTITY_TYPE.valueOf(entity), restToken,
							list.get(index), fieldName + ", id");
					entityApi.update(BHRestApi.Entity.ENTITY_TYPE.valueOf(entity), restToken, list.get(index),
							((ObjectNode) candidate.path("data")).put(fieldName, (int) fieldValue));
					System.out.println(candidate);
				}

			} else {
				for (int index = 0; index < list.size(); index++) {
					if (candidateids.contains(list.get(index).toString())) {
						continue;
					}
					ObjectNode candidate = entityApi.get(BHRestApi.Entity.ENTITY_TYPE.valueOf(entity), restToken,
							list.get(index), fieldName + ", id");
					entityApi.update(BHRestApi.Entity.ENTITY_TYPE.valueOf(entity), restToken, list.get(index),
							((ObjectNode) candidate.path("data")).put(fieldName, (String) fieldValue));
					writer.write(list.get(index).toString());
					writer.newLine();
					writer.flush();
				}
				writer.close();
				// Delete the file candidate.list
				Files.deleteIfExists(candidateFile);
			}
		} catch (final Exception ex) {
			System.exit(10);
		}
	}
}
