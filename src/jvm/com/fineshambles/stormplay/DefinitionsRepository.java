package com.fineshambles.stormplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.basho.riak.pbc.KeySource;
import com.basho.riak.pbc.RiakClient;
import com.basho.riak.pbc.RiakObject;
import com.fineshambles.stormplay.WordProtos.Word;
import com.fineshambles.stormplay.WordProtos.Word.Builder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class DefinitionsRepository {
	private final static String BUCKET = "definitions";
	
	public static List<String> get(String word) throws IOException {
		RiakClient client = getClient();
		RiakObject[] riakObject = client.fetch(BUCKET, word);
		Word[] words = parseWords(riakObject);
		
		if (riakObject.length == 0) {
			return new ArrayList<String>();
		} else if (riakObject.length > 1) {
			List<String> definitions = mergeWords(word, words).getDefinitionsList();
			put(word, definitions);
		    return definitions;
		} else {
			Word wordObject = Word.parseFrom(riakObject[0].getValue());
			return wordObject.getDefinitionsList();
		}
	}

	private static Word[] parseWords(RiakObject[] riakObject)
			throws InvalidProtocolBufferException {
		Word[] words = new Word[riakObject.length];
		for (int i = 0; i < riakObject.length; i++) {
			words[i] = Word.parseFrom(riakObject[i].getValue());
		}
		return words;
	}
	
	private static Word mergeWords(String wordString, Word[] words) {
		Builder builder = Word.newBuilder();
		builder.setWord(wordString);
		for (Word word : words) {
			for (String definition : word.getDefinitionsList()) {
				if (!builder.getDefinitionsList().contains(definition)) {
					builder.addDefinitions(definition);
				}
			}
		}
		return builder.build();
	}

	public static void put(String word, List<String> definitions) throws IOException {
		Word wordObject = Word.newBuilder().setWord(word).addAllDefinitions(definitions).build();
		RiakObject riakObject = new RiakObject(BUCKET, word, wordObject.toByteArray());
		RiakClient client = getClient();
		client.store(riakObject);
	}

	private static RiakClient getClient() throws IOException {
		return new RiakClient("localhost");
	}
	
	public static void asplosion() throws IOException {
		RiakClient client = getClient();
		
		ByteString byteStringBucket = ByteString.copyFromUtf8(BUCKET);
		KeySource keys = client.listKeys(byteStringBucket);
		for (ByteString key : keys) {
			client.delete(byteStringBucket, key);
		}
	}
	
	public static List<String> getWords() throws IOException {
		ArrayList<String> words = new ArrayList<String>();
		RiakClient client = getClient();
		
		ByteString byteStringBucket = ByteString.copyFromUtf8(BUCKET);
		KeySource keys = client.listKeys(byteStringBucket);
		for (ByteString key : keys) {
			String word = parseWords(client.fetch(byteStringBucket, key))[0].getWord();
			words.add(word);
		}
		return words;
	}
}
