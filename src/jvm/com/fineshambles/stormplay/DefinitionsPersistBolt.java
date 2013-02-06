package com.fineshambles.stormplay;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.esotericsoftware.minlog.Log;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

public class DefinitionsPersistBolt extends BaseRichBolt implements IRichBolt {

	private OutputCollector collector;

	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		String word = input.getString(0);
		@SuppressWarnings("unchecked")
		List<String> definitions = (List<String>)input.getValue(1);
		try {
			DefinitionsRepository.put(word, definitions);
		} catch (IOException e) {
			Logger.getLogger(this.getClass()).error("Couldn't save word " + word, e);
			return;
		}
		collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}

}
