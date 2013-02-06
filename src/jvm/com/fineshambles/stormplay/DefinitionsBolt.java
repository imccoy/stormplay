package com.fineshambles.stormplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class DefinitionsBolt extends BaseRichBolt {
	
	
	private OutputCollector collector;
	private ConcurrentHashMap<String, List<String>> map;

	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		this.map = new ConcurrentHashMap<String, List<String>>();
	}

	@Override
	public void execute(Tuple input) {
		String word = input.getString(0);
		@SuppressWarnings("unchecked")
		List<String> newDefinitions = (List<String>)input.getValue(1);
		
		try {
			initWordEntry(word);
		} catch (IOException e) {
			Logger.getLogger(this.getClass()).error("Couldn't load word " + word, e);
			return;
		}
		List<String> definitions = map.get(word);
		List<String> definitionsOut = new ArrayList<String>();
		synchronized (definitions) {
			for (String definition : newDefinitions) {
				if (!definitions.contains(definition))
					definitions.add(definition);
			}
			definitionsOut.addAll(definitions);
		}
		
		ArrayList<Object> tuple = new ArrayList<Object>(2);
		tuple.add(word);
		tuple.add(definitionsOut);
		collector.emit(tuple);
		collector.ack(input);
	}

	private void initWordEntry(String word) throws IOException {
		if (map.containsKey(word))
			return;
		map.putIfAbsent(word, DefinitionsRepository.get(word));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("word", "definitions"));
	}

}
