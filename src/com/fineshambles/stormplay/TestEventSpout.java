package com.fineshambles.stormplay;

import java.util.ArrayList;
import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;

public class TestEventSpout extends BaseRichSpout {

	private SpoutOutputCollector collector;
	private int count = 0;
	private int maxCount = 100;
	private String[] words = new String[] { "Dog", "Cat", "Mouse", "Tiger" };
	private String[] attributes = new String[] { "fierce", "petite", "monstrous", "adorable" };
	private String[] analogies = new String[] { "dragon", "unicorn", "centaur", "phoenix" };


	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void nextTuple() {
		count += 1;
		if (count >= maxCount)
			return;
		ArrayList<Object> tuple = new ArrayList<Object>();
		String word = pickArrayElem(words);
		String definition = "Like a " + pickArrayElem(analogies) +
				               " but more " + pickArrayElem(attributes);
		tuple.add(word);
		tuple.add(definition);
		collector.emit(tuple);
	}
	

	
	private<T> T pickArrayElem(T[] list) {
		int idx = (int)(Math.random() * list.length);
		return list[idx];
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("word", "definition"));
	}

}
