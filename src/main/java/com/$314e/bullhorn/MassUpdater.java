package com.$314e.bullhorn;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
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
	

	public static void main(final String... args) throws Exception {
		
		
		
		
		
		
		
		
		
		logger.entry();
		final DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder("META-INF/config.xml");
		config = factory.getConfiguration();
		for (final Iterator<String> ite = config.getKeys(); ite.hasNext();) {
			final String key = ite.next();
			logger.debug("{}: {}", key, config.getProperty(key));
		}
		logger.exit(); 
		
		token = BHRestUtil.getRestToken( (String) config.getProperty("BH_CLIENT_ID"), (String) config.getProperty("BH_CLIENT_SECRET"),
				(String) config.getProperty("BH_USER"), (String) config.getProperty("BH_PASSWORD"));
		restToken = token.get("BhRestToken").asText();
		entityApi = BHRestUtil.getEntityApi(token);
		
		
		
		Scanner scanner = new Scanner(new File((String) config.getProperty("datafile")));
		
		final ObjectNode data = new ObjectNode(JsonNodeFactory.instance);
		ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
	
		final ArrayNode ids =  data.putArray("ids");
		
		
	
		ArrayList list = new ArrayList();
		
		scanner.useDelimiter(",");
		scanner.nextLine();
		scanner.nextLine();
		while (scanner.hasNext()) {
			if (scanner.hasNextInt()) {
				System.out.println(scanner.nextInt());
			}
			else {
				list.add(scanner.next());
		
			
			
			}	
			scanner.nextLine();
		}
		scanner.close();
		
		
		for(int i = list.size()-1; i>=0; i--) {
			 list.set(i,  Integer.parseInt((String) list.get(i).toString().substring(1, list.get(i).toString().length()-1)));
			ids.add( (int) list.get(i));
		}
		
		String fieldName = (String) config.getProperty("fieldName");
		Object fieldValue = config.getProperty("fieldValue");
		
		data.put((String) config.getProperty("fieldName"), (String) config.getProperty("fieldValue"));
		System.out.println(data);
		
		String entity =  (String) config.getProperty("entityType");
		System.out.println(entity);
		

		
		
		
		System.out.println(data);
		if (fieldName.equals("status") || fieldName.equals("businessSectors") || fieldName.equals("categories") || fieldName.equals("isDeleted") || fieldName.equals("owner") || fieldName.equals("primarySkills") ||
				fieldName.equals("source") || fieldName.equals("specialties")) {
			entityApi.massUpdate(BHRestApi.Entity.ENTITY_TYPE.valueOf(entity), restToken, data);
		} else {
			for (int index = 0; index< list.size(); index++) {
				ObjectNode candidate = entityApi.get(BHRestApi.Entity.ENTITY_TYPE.valueOf(entity), restToken , list.get(index), fieldName + ", id" );
				entityApi.update(BHRestApi.Entity.ENTITY_TYPE.valueOf(entity), restToken, list.get(index),  ((ObjectNode) candidate.path("data")).put(fieldName, (String) fieldValue));
				System.out.println(candidate);		
				
			}
		}
		
		
		
		
	}
}
