package com.fineshambles.stormplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class WordAddedBolt extends BaseRichBolt {

	
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
		String definition = input.getString(1);
		map.putIfAbsent(word, new ArrayList<String>());
		List<String> definitions = map.get(word);
		List<String> definitionsOut = new ArrayList<String>();
		synchronized (definitions) {
			definitions.add(definition);
			definitionsOut.addAll(definitions);
		}
		ArrayList<Object> tuple = new ArrayList<Object>(2);
		tuple.add(word);
		tuple.add(definitionsOut);
		collector.emit(tuple);
		collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("word", "definitions"));
	}

}
