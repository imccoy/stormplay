package com.fineshambles.stormplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import backtype.storm.LocalDRPC;

public class DefinitionsRepository {

	private static LocalDRPC drpc;

	public static List<String> get(String word) throws IOException {
		String jsonString = drpc.execute("definitions-for", word);
		JsonNode definitionsList = getResultList(jsonString);
		return makeStringListFromResult(definitionsList);
	}

	public static List<String> getWords() throws IOException {
		String jsonString = drpc.execute("recent-words", "w");
		JsonNode wordsList = getResultList(jsonString);
		return makeStringListFromResult(wordsList);
	}

	public static ArrayList<String> makeStringListFromResult(JsonNode wordsList) {
		ArrayList<String> list = new ArrayList<String>();
		for (JsonNode wordNode : wordsList) {
			String word = wordNode.getTextValue();
			list.add(word);
		}
		return list;
	}

	public static JsonNode getResultList(String jsonString) throws IOException,
			JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readValue(jsonString, JsonNode.class);
		JsonNode wordsList = rootNode.get(0).get(1);
		return wordsList;
	}
	

	public static void connect(LocalDRPC drpc) {
		DefinitionsRepository.drpc = drpc;
	}
}
